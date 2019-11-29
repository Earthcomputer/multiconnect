package net.earthcomputer.multiconnect.protocols.v1_13_2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.impl.TransformerByteBuf;
import net.earthcomputer.multiconnect.protocols.v1_13_2.mixin.PendingDifficulty;
import net.earthcomputer.multiconnect.protocols.v1_14.Protocol_1_14;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.packet.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.server.network.packet.*;
import net.minecraft.util.Hand;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.TagHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class Protocol_1_13_2 extends Protocol_1_14 {

    private static final Palette<BlockState> BLOCK_STATE_PALETTE;
    static {
        try {
            Field field = Arrays.stream(ChunkSection.class.getDeclaredFields())
                    .filter(f -> f.getType() == Palette.class)
                    .findFirst().orElseThrow(NoSuchFieldException::new);
            field.setAccessible(true);
            //noinspection unchecked
            BLOCK_STATE_PALETTE = (Palette<BlockState>) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public List<Class<? extends Packet<?>>> getClientboundPackets() {
        List<Class<? extends Packet<?>>> packets = super.getClientboundPackets();
        insertAfter(packets, GuiCloseS2CPacket.class, GuiOpenS2CPacket_1_13_2.class);
        packets.remove(TagQueryResponseS2CPacket.class);
        insertAfter(packets, EntityStatusS2CPacket.class, TagQueryResponseS2CPacket.class);
        packets.remove(GuiOpenS2CPacket.class);
        packets.remove(LightUpdateS2CPacket.class);
        packets.remove(EntityS2CPacket.class);
        insertAfter(packets, SetTradeOffersPacket.class, EntityS2CPacket.class);
        packets.remove(SetTradeOffersPacket.class);
        packets.remove(OpenWrittenBookS2CPacket.class);
        packets.remove(OpenContainerPacket.class);
        insertAfter(packets, PlayerPositionLookS2CPacket.class, UseBedS2CPacket.class);
        packets.remove(ChunkRenderDistanceCenterS2CPacket.class);
        packets.remove(ChunkLoadDistanceS2CPacket.class);
        packets.remove(StopSoundS2CPacket.class);
        insertAfter(packets, PlaySoundFromEntityS2CPacket.class, StopSoundS2CPacket.class);
        packets.remove(PlaySoundFromEntityS2CPacket.class);
        return packets;
    }

    @Override
    public List<Class<? extends Packet<?>>> getServerboundPackets() {
        List<Class<? extends Packet<?>>> packets = super.getServerboundPackets();
        packets.remove(UpdateDifficultyC2SPacket.class);
        packets.remove(PlayerMoveC2SPacket.class);
        insertAfter(packets, UpdateDifficultyLockC2SPacket.class, PlayerMoveC2SPacket.class);
        packets.remove(UpdateDifficultyLockC2SPacket.class);
        packets.remove(UpdateJigsawC2SPacket.class);
        return packets;
    }

    @Override
    public void transformPacketClientbound(Class<? extends Packet<?>> packetClass, List<TransformerByteBuf> transformers) {
        super.transformPacketClientbound(packetClass, transformers);

        if (packetClass == ChunkDataS2CPacket.class) {
            transformers.add(new TransformerByteBuf() {
                private boolean hasReadHeighmaps = false;
                private boolean hasReadVerticalStripBitmask = false;
                private int verticalStripBitmask;

                @Override
                public CompoundTag readCompoundTag() {
                    if (!isTopLevel()) {
                        return super.readCompoundTag();
                    }
                    if (!hasReadHeighmaps) {
                        hasReadHeighmaps = true;
                        return new CompoundTag();
                    }
                    return super.readCompoundTag();
                }

                @Override
                public int readVarInt() {
                    if (!isTopLevel()) {
                        return super.readVarInt();
                    }
                    if (!hasReadVerticalStripBitmask) {
                        hasReadVerticalStripBitmask = true;
                        verticalStripBitmask = super.readVarInt();
                        return verticalStripBitmask;
                    }
                    return super.readVarInt();
                }

                @Override
                public ByteBuf readBytes(byte[] data) {
                    if (!isTopLevel())
                        return super.readBytes(data);

                    super.readBytes(data);
                    PacketByteBuf inBuf = new PacketByteBuf(Unpooled.wrappedBuffer(data.clone()));
                    PalettedContainer<BlockState> tempContainer = new PalettedContainer<>(BLOCK_STATE_PALETTE, Block.STATE_IDS, TagHelper::deserializeBlockState, TagHelper::serializeBlockState, Blocks.AIR.getDefaultState());
                    PacketByteBuf outBuf = new PacketByteBuf(Unpooled.wrappedBuffer(data));
                    outBuf.writerIndex(0);

                    PendingLightData lightData = new PendingLightData();
                    PendingLightData.setInstance(lightData);

                    for (int sectionY = 0; sectionY < 16; sectionY++) {
                        if ((verticalStripBitmask & (1 << sectionY)) != 0) {
                            outBuf.writeShort(0); // non-empty block count
                            tempContainer.fromPacket(inBuf);
                            tempContainer.toPacket(outBuf);
                            byte[] light = new byte[16 * 16 * 16 / 2];
                            inBuf.readBytes(light);
                            lightData.setBlockLight(sectionY, light);
                            if (MinecraftClient.getInstance().world.dimension.hasSkyLight()) {
                                light = new byte[16 * 16 * 16 / 2];
                                inBuf.readBytes(light);
                                lightData.setSkyLight(sectionY, light);
                            }
                        }
                    }
                    return this;
                }
            });
        }
        else if (packetClass == GameJoinS2CPacket.class) {
            transformers.add(new TransformerByteBuf() {
                private int intsRead = 0;

                @Override
                public int readInt() {
                    if (!isTopLevel() || intsRead++ != 1)
                        return super.readInt();
                    int ret = super.readInt();
                    PendingDifficulty.setPendingDifficulty(Difficulty.byOrdinal(readUnsignedByte()));
                    return ret;
                }

                @Override
                public int readVarInt() {
                    return isTopLevel() ? 64 : super.readVarInt();
                }
            });
        }
        else if (packetClass == MapUpdateS2CPacket.class) {
            transformers.add(new TransformerByteBuf() {
                private int booleansRead = 0;

                @Override
                public boolean readBoolean() {
                    if (!isTopLevel() || booleansRead++ != 1) {
                        return super.readBoolean();
                    }
                    return false;
                }
            });
        }
        else if (packetClass == EntityAttachS2CPacket.class) {
            transformers.add(new TransformerByteBuf() {
                private int intsRead = 0;

                @Override
                public int readInt() {
                    if (!isTopLevel() || intsRead++ != 1) {
                        return super.readInt();
                    }
                    int ret = super.readInt();
                    if (ret == -1) ret = 0;
                    return ret;
                }
            });
        }
        else if (packetClass == PlayerRespawnS2CPacket.class) {
            transformers.add(new TransformerByteBuf() {
                @Override
                public short readUnsignedByte() {
                    if (!isTopLevel())
                        return super.readUnsignedByte();
                    PendingDifficulty.setPendingDifficulty(Difficulty.byOrdinal(super.readUnsignedByte()));
                    return super.readUnsignedByte();
                }
            });
        }
        else if (packetClass == DifficultyS2CPacket.class) {
            transformers.add(new TransformerByteBuf() {
                @Override
                public boolean readBoolean() {
                    if (!isTopLevel())
                        return super.readBoolean();
                    return false;
                }
            });
        }
        else if (packetClass == EntitySpawnS2CPacket.class) {
            transformers.add(new TransformerByteBuf() {
                private int varIntsRead = 0;

                @Override
                public int readVarInt() {
                    if (!isTopLevel() || varIntsRead++ != 1)
                        return super.readVarInt();
                    return super.readByte() & 0xff;
                }
            });
        }
    }

    @Override
    public void transformPacketServerbound(Class<? extends Packet<?>> packetClass, List<TransformerByteBuf> transformers) {
        super.transformPacketServerbound(packetClass, transformers);

        if (packetClass == PlayerInteractBlockC2SPacket.class) {
            transformers.add(new TransformerByteBuf() {
                private Hand hand = null;

                @Override
                public PacketByteBuf writeEnumConstant(Enum<?> val) {
                    if (hand == null && isTopLevel() && val.getClass() == Hand.class)
                        this.hand = (Hand) val;
                    else
                        super.writeEnumConstant(val);
                    return this;
                }

                @Override
                public void writeBlockHitResult(BlockHitResult hitResult) {
                    BlockPos hitBlockPos = hitResult.getBlockPos();
                    Vec3d hitPos = hitResult.getPos();
                    writeBlockPos(hitBlockPos);
                    writeEnumConstant(hitResult.getSide());
                    writeEnumConstant(hand);
                    writeFloat((float) (hitPos.x - hitBlockPos.getX()));
                    writeFloat((float) (hitPos.y - hitBlockPos.getY()));
                    writeFloat((float) (hitPos.z - hitBlockPos.getZ()));
                }
            });
        }
        else if (packetClass == RecipeBookDataC2SPacket.class) {
            transformers.add(new TransformerByteBuf() {
                private int booleansWritten = 0;

                @Override
                public ByteBuf writeBoolean(boolean val) {
                    if (isTopLevel() || booleansWritten++ < 4)
                        super.writeBoolean(val);
                    return this;
                }
            });
        }
    }

    @SuppressWarnings({"EqualsBetweenInconvertibleTypes", "unchecked"})
    @Override
    public void modifyRegistry(ISimpleRegistry<?> registry) {
        super.modifyRegistry(registry);

        if (registry == Registry.BLOCK) {
            modifyBlockRegistry((ISimpleRegistry<Block>) registry);
        } else if (registry == Registry.ITEM) {
            modifyItemRegistry((ISimpleRegistry<Item>) registry);
        } else if (registry == Registry.ENTITY_TYPE) {
            modifyEntityTypeRegistry((ISimpleRegistry<EntityType<?>>) registry);
        } else if (registry == Registry.BIOME) {
            modifyBiomeRegistry((ISimpleRegistry<Biome>) registry);
        } else if (registry == Registry.STATUS_EFFECT) {
            modifyStatusEffectRegistry((ISimpleRegistry<StatusEffect>) registry);
        } else if (registry == Registry.PARTICLE_TYPE) {
            modifyParticleTypeRegistry((ISimpleRegistry<ParticleType<? extends ParticleEffect>>) registry);
        } else if (registry == Registry.ENCHANTMENT) {
            modifyEnchantmentRegistry((ISimpleRegistry<Enchantment>) registry);
        } else if (registry == Registry.BLOCK_ENTITY) {
            modifyBlockEntityRegistry((ISimpleRegistry<BlockEntityType<?>>) registry);
        } else if (registry == Registry.RECIPE_SERIALIZER) {
            modifyRecipeSerializerRegistry((ISimpleRegistry<RecipeSerializer<?>>) registry);
        }
    }

    private void modifyBlockRegistry(ISimpleRegistry<Block> registry) {
        registry.unregister(Blocks.BAMBOO);
        registry.unregister(Blocks.BAMBOO_SAPLING);
        registry.unregister(Blocks.POTTED_BAMBOO);
        registry.unregister(Blocks.BARREL);
        registry.unregister(Blocks.BELL);
        registry.unregister(Blocks.BLAST_FURNACE);
        registry.unregister(Blocks.CAMPFIRE);
        registry.unregister(Blocks.CARTOGRAPHY_TABLE);
        registry.unregister(Blocks.COMPOSTER);
        registry.unregister(Blocks.FLETCHING_TABLE);
        registry.unregister(Blocks.CORNFLOWER);
        registry.unregister(Blocks.LILY_OF_THE_VALLEY);
        registry.unregister(Blocks.WITHER_ROSE);
        registry.unregister(Blocks.GRINDSTONE);
        registry.unregister(Blocks.JIGSAW);
        registry.unregister(Blocks.LANTERN);
        registry.unregister(Blocks.LECTERN);
        registry.unregister(Blocks.LOOM);
        registry.unregister(Blocks.SCAFFOLDING);
        registry.unregister(Blocks.SPRUCE_SIGN);
        registry.unregister(Blocks.SPRUCE_WALL_SIGN);
        registry.unregister(Blocks.BIRCH_SIGN);
        registry.unregister(Blocks.BIRCH_WALL_SIGN);
        registry.unregister(Blocks.JUNGLE_SIGN);
        registry.unregister(Blocks.JUNGLE_WALL_SIGN);
        registry.unregister(Blocks.ACACIA_SIGN);
        registry.unregister(Blocks.ACACIA_WALL_SIGN);
        registry.unregister(Blocks.DARK_OAK_SIGN);
        registry.unregister(Blocks.DARK_OAK_WALL_SIGN);
        registry.unregister(Blocks.STONE_SLAB);
        registry.unregister(Blocks.STONE_STAIRS);
        registry.unregister(Blocks.ANDESITE_SLAB);
        registry.unregister(Blocks.ANDESITE_STAIRS);
        registry.unregister(Blocks.POLISHED_ANDESITE_SLAB);
        registry.unregister(Blocks.POLISHED_ANDESITE_STAIRS);
        registry.unregister(Blocks.DIORITE_SLAB);
        registry.unregister(Blocks.DIORITE_STAIRS);
        registry.unregister(Blocks.POLISHED_DIORITE_SLAB);
        registry.unregister(Blocks.POLISHED_DIORITE_STAIRS);
        registry.unregister(Blocks.GRANITE_SLAB);
        registry.unregister(Blocks.GRANITE_STAIRS);
        registry.unregister(Blocks.POLISHED_GRANITE_SLAB);
        registry.unregister(Blocks.POLISHED_GRANITE_STAIRS);
        registry.unregister(Blocks.MOSSY_STONE_BRICK_SLAB);
        registry.unregister(Blocks.MOSSY_STONE_BRICK_STAIRS);
        registry.unregister(Blocks.MOSSY_COBBLESTONE_SLAB);
        registry.unregister(Blocks.MOSSY_COBBLESTONE_STAIRS);
        registry.unregister(Blocks.SMOOTH_SANDSTONE_SLAB);
        registry.unregister(Blocks.SMOOTH_SANDSTONE_STAIRS);
        registry.unregister(Blocks.CUT_SANDSTONE_SLAB);
        registry.unregister(Blocks.SMOOTH_RED_SANDSTONE_SLAB);
        registry.unregister(Blocks.SMOOTH_RED_SANDSTONE_STAIRS);
        registry.unregister(Blocks.CUT_RED_SANDSTONE_SLAB);
        registry.unregister(Blocks.SMOOTH_QUARTZ_SLAB);
        registry.unregister(Blocks.SMOOTH_QUARTZ_STAIRS);
        registry.unregister(Blocks.RED_NETHER_BRICK_SLAB);
        registry.unregister(Blocks.RED_NETHER_BRICK_STAIRS);
        registry.unregister(Blocks.END_STONE_BRICK_SLAB);
        registry.unregister(Blocks.END_STONE_BRICK_STAIRS);
        registry.unregister(Blocks.SMITHING_TABLE);
        registry.unregister(Blocks.SMOKER);
        registry.unregister(Blocks.STONECUTTER);
        registry.unregister(Blocks.SWEET_BERRY_BUSH);
        registry.unregister(Blocks.BRICK_WALL);
        registry.unregister(Blocks.ANDESITE_WALL);
        registry.unregister(Blocks.DIORITE_WALL);
        registry.unregister(Blocks.GRANITE_WALL);
        registry.unregister(Blocks.PRISMARINE_WALL);
        registry.unregister(Blocks.STONE_BRICK_WALL);
        registry.unregister(Blocks.MOSSY_STONE_BRICK_WALL);
        registry.unregister(Blocks.SANDSTONE_WALL);
        registry.unregister(Blocks.RED_SANDSTONE_WALL);
        registry.unregister(Blocks.NETHER_BRICK_WALL);
        registry.unregister(Blocks.RED_NETHER_BRICK_WALL);
        registry.unregister(Blocks.END_STONE_BRICK_WALL);
    }

    private void modifyItemRegistry(ISimpleRegistry<Item> registry) {
        registry.unregister(Items.CROSSBOW);
        registry.unregister(Items.BLUE_DYE);
        registry.unregister(Items.BROWN_DYE);
        registry.unregister(Items.BLACK_DYE);
        registry.unregister(Items.WHITE_DYE);
        registry.unregister(Items.LEATHER_HORSE_ARMOR);
        registry.unregister(Items.SUSPICIOUS_STEW);
        registry.unregister(Items.SWEET_BERRIES);
    }

    private void modifyEntityTypeRegistry(ISimpleRegistry<EntityType<?>> registry) {
        registry.unregister(EntityType.CAT);
        registry.unregister(EntityType.FOX);
        registry.unregister(EntityType.PANDA);
        registry.unregister(EntityType.PILLAGER);
        registry.unregister(EntityType.RAVAGER);
        registry.unregister(EntityType.TRADER_LLAMA);
        registry.unregister(EntityType.WANDERING_TRADER);
    }

    private void modifyBiomeRegistry(ISimpleRegistry<Biome> registry) {
        registry.unregister(Biomes.BAMBOO_JUNGLE);
        registry.unregister(Biomes.BAMBOO_JUNGLE_HILLS);
    }

    private void modifyStatusEffectRegistry(ISimpleRegistry<StatusEffect> registry) {
        registry.unregister(StatusEffects.BAD_OMEN);
        registry.unregister(StatusEffects.HERO_OF_THE_VILLAGE);
    }

    private void modifyParticleTypeRegistry(ISimpleRegistry<ParticleType<? extends ParticleEffect>> registry) {
        registry.unregister(ParticleTypes.CAMPFIRE_COSY_SMOKE);
        registry.unregister(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE);
        registry.unregister(ParticleTypes.SNEEZE);
    }

    private void modifyEnchantmentRegistry(ISimpleRegistry<Enchantment> registry) {
        registry.unregister(Enchantments.QUICK_CHARGE);
        registry.unregister(Enchantments.MULTISHOT);
        registry.unregister(Enchantments.PIERCING);
    }

    private void modifyBlockEntityRegistry(ISimpleRegistry<BlockEntityType<?>> registry) {
        registry.unregister(BlockEntityType.BARREL);
        registry.unregister(BlockEntityType.SMOKER);
        registry.unregister(BlockEntityType.BLAST_FURNACE);
        registry.unregister(BlockEntityType.LECTERN);
        registry.unregister(BlockEntityType.BELL);
        registry.unregister(BlockEntityType.JIGSAW);
        registry.unregister(BlockEntityType.CAMPFIRE);
    }

    private void modifyRecipeSerializerRegistry(ISimpleRegistry<RecipeSerializer<?>> registry) {
        registry.unregister(RecipeSerializer.SUSPICIOUS_STEW);
        registry.unregister(RecipeSerializer.BLASTING);
        registry.unregister(RecipeSerializer.SMOKING);
        registry.unregister(RecipeSerializer.CAMPFIRE_COOKING);
        registry.unregister(RecipeSerializer.STONECUTTING);
    }
}
