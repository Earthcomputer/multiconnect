package net.earthcomputer.multiconnect.protocols.v1_13_2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.earthcomputer.multiconnect.impl.DataTrackerManager;
import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.impl.TransformerByteBuf;
import net.earthcomputer.multiconnect.protocols.v1_14.Protocol_1_14;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.packet.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.server.network.packet.*;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.TagHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.Difficulty;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class Protocol_1_13_2 extends Protocol_1_14 {

    private static final Field ENTITY_POSE = DataTrackerManager.getTrackedDataField(Entity.class, 6, "POSE");
    private static final Field ENDER_EYE_ITEM = DataTrackerManager.getTrackedDataField(EnderEyeEntity.class, 0, "ITEM");
    private static final Field FIREWORK_SHOOTER = DataTrackerManager.getTrackedDataField(FireworkEntity.class, 1, "SHOOTER_ENTITY_ID");
    private static final Field FIREWORK_ANGLE = DataTrackerManager.getTrackedDataField(FireworkEntity.class, 2, "SHOT_AT_ANGLE");
    private static final Field LIVING_SLEEPING_POSITION = DataTrackerManager.getTrackedDataField(LivingEntity.class, 5, "SLEEPING_POSITION");
    private static final Field VILLAGER_DATA = DataTrackerManager.getTrackedDataField(VillagerEntity.class, 0, "VILLAGER_DATA");
    private static final Field ZOMBIE_DROWNING = DataTrackerManager.getTrackedDataField(ZombieEntity.class, 2, "CONVERTING_IN_WATER");
    private static final Field ZOMBIE_VILLAGER_DATA = DataTrackerManager.getTrackedDataField(ZombieVillagerEntity.class, 1, "VILLAGER_DATA");
    private static final Field MOOSHROOM_TYPE = DataTrackerManager.getTrackedDataField(MooshroomEntity.class, 0, "TYPE");
    private static final Field CAT_SLEEPING_WITH_OWNER = DataTrackerManager.getTrackedDataField(CatEntity.class, 1, "SLEEPING_WITH_OWNER");
    private static final Field CAT_HEAD_DOWN = DataTrackerManager.getTrackedDataField(CatEntity.class, 2, "HEAD_DOWN");
    private static final Field CAT_COLLAR_COLOR = DataTrackerManager.getTrackedDataField(CatEntity.class, 3, "COLLAR_COLOR");
    private static final Field PROJECTILE_PIERCE_LEVEL = DataTrackerManager.getTrackedDataField(ProjectileEntity.class, 2, "PIERCE_LEVEL");

    private static final TrackedData<Integer> OLD_FIREWORK_SHOOTER = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> OLD_VILLAGER_PROFESSION = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Byte> OLD_ILLAGER_FLAGS = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Boolean> OLD_SKELETON_ATTACKING = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> OLD_ZOMBIE_ATTACKING = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> OLD_ZOMBIE_VILLAGER_PROFESSION = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> OLD_HORSE_ARMOR = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);

    private static SimpleRegistry<EntityType<?>> ENTITY_REGISTRY_1_13;

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
                private int chunkX, chunkZ;
                private int intsRead = 0;
                private boolean hasReadHeighmaps = false;
                private boolean hasReadVerticalStripBitmask = false;
                private int verticalStripBitmask;
                private boolean isFullChunk;
                private int booleansRead = 0;

                @Override
                public boolean readBoolean() {
                    if (!isTopLevel() || booleansRead++ != 0)
                        return super.readBoolean();
                    isFullChunk = super.readBoolean();
                    return isFullChunk;
                }

                @Override
                public int readInt() {
                    if (!isTopLevel())
                        return super.readInt();
                    int val = super.readInt();
                    switch (intsRead++) {
                        case 0:
                            chunkX = val;
                            break;
                        case 1:
                            chunkZ = val;
                            break;
                    }
                    return val;
                }

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
                    PendingLightData.setInstance(chunkX, chunkZ, lightData);

                    for (int sectionY = 0; sectionY < 18; sectionY++) {
                        if ((verticalStripBitmask & (1 << sectionY)) != 0) {
                            outBuf.writeShort(0); // non-empty block count
                            tempContainer.fromPacket(inBuf);
                            tempContainer.toPacket(outBuf);
                            byte[] light = new byte[16 * 16 * 16 / 2];
                            inBuf.readBytes(light);
                            lightData.setBlockLight(sectionY, light);
                            // since this thread is async, this code may be run before the join game packet is
                            // processed, and therefore world would be null
                            while (MinecraftClient.getInstance().world == null) {
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException e) {
                                    return this;
                                }
                            }
                            if (MinecraftClient.getInstance().world.dimension.hasSkyLight()) {
                                light = new byte[16 * 16 * 16 / 2];
                                inBuf.readBytes(light);
                                lightData.setSkyLight(sectionY, light);
                            }
                        }
                    }

                    // biomes
                    if (isFullChunk) {
                        for (int i = 0; i < 256; i++)
                            outBuf.writeInt(inBuf.readInt());
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
                private double x, y, z;
                private byte pitch, yaw;
                private int entityData;
                private int doublesRead, bytesRead, intsRead;

                @Override
                public byte readByte() {
                    if (!isTopLevel())
                        return super.readByte();
                    switch (bytesRead++) {
                        case 0:
                            return pitch;
                        case 1:
                            return yaw;
                    }
                    return super.readByte();
                }

                @Override
                public int readInt() {
                    if (!isTopLevel() || intsRead++ != 0)
                        return super.readInt();
                    return entityData;
                }

                @Override
                public double readDouble() {
                    if (!isTopLevel())
                        return super.readDouble();
                    switch (doublesRead++) {
                        case 0:
                            return x;
                        case 1:
                            return y;
                        case 2:
                            return z;
                    }
                    return super.readDouble();
                }

                @Override
                public int readVarInt() {
                    if (!isTopLevel() || varIntsRead++ != 1)
                        return super.readVarInt();
                    int typeId = super.readByte() & 0xff;
                    x = super.readDouble();
                    y = super.readDouble();
                    z = super.readDouble();
                    pitch = super.readByte();
                    yaw = super.readByte();
                    entityData = super.readInt();
                    return Registry.ENTITY_TYPE.getRawId(mapObjectId(typeId, entityData));
                }
            });
        }
        else if (packetClass == SynchronizeRecipesS2CPacket.class) {
            transformers.add(new TransformerByteBuf() {
                private Identifier recipeOutput;
                private int identifiersRead = 0;

                @Override
                public void setUserData(int val) {
                    identifiersRead = 0;
                }

                @Override
                public Identifier readIdentifier() {
                    if (!isTopLevel())
                        return super.readIdentifier();
                    switch (identifiersRead++) {
                        case 0:
                            recipeOutput = super.readIdentifier();
                            return new Identifier(readString(32767));
                        case 1:
                            return recipeOutput;
                        default:
                            return super.readIdentifier();
                    }
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

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == Entity.class && data == DataTrackerManager.getTrackedData(EntityPose.class, ENTITY_POSE))
            return false;
        if (clazz == EnderEyeEntity.class && data == DataTrackerManager.getTrackedData(ItemStack.class, ENDER_EYE_ITEM))
            return false;
        if (clazz == FireworkEntity.class) {
            TrackedData<OptionalInt> fireworkShooter = DataTrackerManager.getTrackedData(OptionalInt.class, FIREWORK_SHOOTER);
            if (data == fireworkShooter) {
                DataTrackerManager.registerOldTrackedData(FireworkEntity.class, OLD_FIREWORK_SHOOTER, 0,
                        (entity, val) -> entity.getDataTracker().set(fireworkShooter, val <= 0 ? OptionalInt.empty() : OptionalInt.of(val)));
                return false;
            }
            if (data == DataTrackerManager.getTrackedData(Boolean.class, FIREWORK_ANGLE))
                return false;
        }
        if (clazz == LivingEntity.class && data == DataTrackerManager.getTrackedData(Optional.class, LIVING_SLEEPING_POSITION))
            return false;
        if (clazz == VillagerEntity.class) {
            TrackedData<VillagerData> villagerData = DataTrackerManager.getTrackedData(VillagerData.class, VILLAGER_DATA);
            if (data == villagerData) {
                DataTrackerManager.registerOldTrackedData(VillagerEntity.class, OLD_VILLAGER_PROFESSION, 0,
                        (entity, val) -> entity.getDataTracker().set(villagerData, entity.getVillagerData().withProfession(getVillagerProfession(val))));
                return false;
            }
        }
        if (clazz == ZombieEntity.class && data == DataTrackerManager.getTrackedData(Boolean.class, ZOMBIE_DROWNING))
            DataTrackerManager.registerOldTrackedData(ZombieEntity.class, OLD_ZOMBIE_ATTACKING, false, MobEntity::setAttacking);
        if (clazz == ZombieVillagerEntity.class) {
            TrackedData<VillagerData> villagerData = DataTrackerManager.getTrackedData(VillagerData.class, ZOMBIE_VILLAGER_DATA);
            if (data == villagerData) {
                DataTrackerManager.registerOldTrackedData(ZombieVillagerEntity.class, OLD_ZOMBIE_VILLAGER_PROFESSION, 0,
                        (entity, val) -> entity.getDataTracker().set(villagerData, entity.getVillagerData().withProfession(getVillagerProfession(val))));
                return false;
            }
        }
        if (clazz == MooshroomEntity.class && data == DataTrackerManager.getTrackedData(String.class, MOOSHROOM_TYPE))
            return false;
        if (clazz == CatEntity.class) {
            if (data == DataTrackerManager.getTrackedData(Boolean.class, CAT_SLEEPING_WITH_OWNER)
                || data == DataTrackerManager.getTrackedData(Boolean.class, CAT_HEAD_DOWN)
                || data == DataTrackerManager.getTrackedData(Integer.class, CAT_COLLAR_COLOR))
                return false;
        }
        if (clazz == ProjectileEntity.class && data == DataTrackerManager.getTrackedData(Byte.class, PROJECTILE_PIERCE_LEVEL))
            return false;
        return super.acceptEntityData(clazz, data);
    }

    @Override
    public void postEntityDataRegister(Class<? extends Entity> clazz) {
        if (clazz == IllagerEntity.class)
            DataTrackerManager.registerOldTrackedData(IllagerEntity.class, OLD_ILLAGER_FLAGS, (byte)0,
                    (entity, val) -> entity.setAttacking((val & 1) != 0));
        if (clazz == AbstractSkeletonEntity.class)
            DataTrackerManager.registerOldTrackedData(AbstractSkeletonEntity.class, OLD_SKELETON_ATTACKING, false, MobEntity::setAttacking);
        if (clazz == HorseEntity.class)
            DataTrackerManager.registerOldTrackedData(HorseEntity.class, OLD_HORSE_ARMOR, 0, (entity, val) -> {
                switch (val) {
                    case 1:
                        entity.setEquippedStack(EquipmentSlot.CHEST, new ItemStack(Items.IRON_HORSE_ARMOR));
                        break;
                    case 2:
                        entity.setEquippedStack(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_HORSE_ARMOR));
                        break;
                    case 3:
                        entity.setEquippedStack(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_HORSE_ARMOR));
                        break;
                    default:
                        entity.setEquippedStack(EquipmentSlot.CHEST, ItemStack.EMPTY);
                }
            });
        super.postEntityDataRegister(clazz);
    }

    private static EntityType<?> mapObjectId(int id, int entityData) {
        switch (id) {
            case 10:
                switch (entityData) {
                    case 1:
                        return EntityType.CHEST_MINECART;
                    case 2:
                        return EntityType.FURNACE_MINECART;
                    case 3:
                        return EntityType.TNT_MINECART;
                    case 4:
                        return EntityType.SPAWNER_MINECART;
                    case 5:
                        return EntityType.HOPPER_MINECART;
                    case 6:
                        return EntityType.COMMAND_BLOCK_MINECART;
                    case 0:
                    default:
                        return EntityType.MINECART;
                }
            case 90:
                return EntityType.FISHING_BOBBER;
            case 60:
                return EntityType.ARROW;
            case 91:
                return EntityType.SPECTRAL_ARROW;
            case 94:
                return EntityType.TRIDENT;
            case 61:
                return EntityType.SNOWBALL;
            case 68:
                return EntityType.LLAMA_SPIT;
            case 71:
                return EntityType.ITEM_FRAME;
            case 77:
                return EntityType.LEASH_KNOT;
            case 65:
                return EntityType.ENDER_PEARL;
            case 72:
                return EntityType.EYE_OF_ENDER;
            case 76:
                return EntityType.FIREWORK_ROCKET;
            case 63:
                return EntityType.FIREBALL;
            case 93:
                return EntityType.DRAGON_FIREBALL;
            case 64:
                return EntityType.SMALL_FIREBALL;
            case 66:
                return EntityType.WITHER_SKULL;
            case 67:
                return EntityType.SHULKER_BULLET;
            case 62:
                return EntityType.EGG;
            case 79:
                return EntityType.EVOKER_FANGS;
            case 73:
                return EntityType.POTION;
            case 75:
                return EntityType.EXPERIENCE_BOTTLE;
            case 1:
                return EntityType.BOAT;
            case 50:
                return EntityType.TNT;
            case 78:
                return EntityType.ARMOR_STAND;
            case 51:
                return EntityType.END_CRYSTAL;
            case 2:
                return EntityType.ITEM;
            case 70:
                return EntityType.FALLING_BLOCK;
            case 3:
                return EntityType.AREA_EFFECT_CLOUD;
            default:
                return ENTITY_REGISTRY_1_13.get(id);
        }
    }

    private static VillagerProfession getVillagerProfession(int id) {
        switch (id) {
            case 0:
                return VillagerProfession.FARMER;
            case 1:
                return VillagerProfession.LIBRARIAN;
            case 2:
                return VillagerProfession.CLERIC;
            case 3:
                return VillagerProfession.ARMORER;
            case 4:
                return VillagerProfession.BUTCHER;
            case 5:
            default:
                return VillagerProfession.NITWIT;
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
        int ocelotId = Registry.ENTITY_TYPE.getRawId(EntityType.OCELOT);
        registry.unregister(EntityType.OCELOT);
        registry.register(EntityType.CAT, ocelotId, new Identifier("ocelot"));
        registry.unregister(EntityType.FOX);
        registry.unregister(EntityType.PANDA);
        registry.unregister(EntityType.PILLAGER);
        registry.unregister(EntityType.RAVAGER);
        registry.unregister(EntityType.TRADER_LLAMA);
        registry.unregister(EntityType.WANDERING_TRADER);
        registry.unregister(EntityType.TRIDENT);
        insertAfter(registry, EntityType.POTION, EntityType.TRIDENT, "trident");
        ENTITY_REGISTRY_1_13 = registry.copy();
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
        registry.register(AddBannerPatternRecipe.SERIALIZER, registry.getNextId(), new Identifier("crafting_special_banneraddpattern"));
    }
}
