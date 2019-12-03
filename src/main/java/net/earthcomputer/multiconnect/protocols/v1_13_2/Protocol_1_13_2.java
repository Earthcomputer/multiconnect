package net.earthcomputer.multiconnect.protocols.v1_13_2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.earthcomputer.multiconnect.impl.DataTrackerManager;
import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.impl.TransformerByteBuf;
import net.earthcomputer.multiconnect.protocols.v1_14.Protocol_1_14;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.Instrument;
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
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
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
                    PacketByteBuf outBuf = new PacketByteBuf(Unpooled.wrappedBuffer(data));
                    outBuf.writerIndex(0);

                    PendingLightData lightData = new PendingLightData();
                    PendingLightData.setInstance(chunkX, chunkZ, lightData);

                    for (int sectionY = 0; sectionY < 18; sectionY++) {
                        if ((verticalStripBitmask & (1 << sectionY)) != 0) {
                            outBuf.writeShort(0); // non-empty block count
                            PalettedContainer<BlockState> tempContainer = new PalettedContainer<>(BLOCK_STATE_PALETTE, Block.STATE_IDS, TagHelper::deserializeBlockState, TagHelper::serializeBlockState, Blocks.AIR.getDefaultState());
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

        transformers.add(new TransformerByteBuf() {
            @Override
            public BlockPos readBlockPos() {
                long val = getNotTopLevel(super::readLong);
                int x = (int) (val >> 38);
                int y = (int) (val << 26 >> 52);
                int z = (int) (val << 38 >> 38);
                return new BlockPos(x, y, z);
            }
        });
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

        transformers.add(new TransformerByteBuf() {
            @Override
            public PacketByteBuf writeBlockPos(BlockPos pos) {
                long val = (((long) pos.getX() & 0x3FFFFFF) << 38)
                        | (((long) pos.getY() & 0xFFF) << 26)
                        | ((long) pos.getZ() & 0x3FFFFFF);
                doNotTopLevel(() -> super.writeLong(val));
                return this;
            }
        });
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
        } else if (registry == Registry.SOUND_EVENT) {
            modifySoundEventRegistry((ISimpleRegistry<SoundEvent>) registry);
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
        registry.unregister(Blocks.POTTED_CORNFLOWER);
        registry.unregister(Blocks.POTTED_LILY_OF_THE_VALLEY);
        registry.unregister(Blocks.POTTED_WITHER_ROSE);
        registry.unregister(Blocks.GRINDSTONE);
        registry.unregister(Blocks.JIGSAW);
        registry.unregister(Blocks.LANTERN);
        registry.unregister(Blocks.LECTERN);
        registry.unregister(Blocks.LOOM);
        registry.unregister(Blocks.SCAFFOLDING);
        rename(registry, Blocks.OAK_SIGN, "sign");
        rename(registry, Blocks.OAK_WALL_SIGN, "wall_sign");
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

        registry.unregister(Blocks.STONE_BRICKS);
        registry.unregister(Blocks.MOSSY_STONE_BRICKS);
        registry.unregister(Blocks.CRACKED_STONE_BRICKS);
        registry.unregister(Blocks.CHISELED_STONE_BRICKS);
        insertAfter(registry, Blocks.INFESTED_CHISELED_STONE_BRICKS, Blocks.STONE_BRICKS, "stone_bricks");
        insertAfter(registry, Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, "mossy_stone_bricks");
        insertAfter(registry, Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, "cracked_stone_bricks");
        insertAfter(registry, Blocks.CRACKED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS, "chiseled_stone_bricks");

        registry.unregister(Blocks.SKELETON_WALL_SKULL);
        insertAfter(registry, Blocks.DARK_OAK_BUTTON, Blocks.SKELETON_WALL_SKULL, "skeleton_wall_skull");
        registry.unregister(Blocks.WITHER_SKELETON_WALL_SKULL);
        insertAfter(registry, Blocks.SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, "wither_skeleton_wall_skull");
        registry.unregister(Blocks.ZOMBIE_WALL_HEAD);
        insertAfter(registry, Blocks.WITHER_SKELETON_SKULL, Blocks.ZOMBIE_WALL_HEAD, "zombie_wall_head");
        registry.unregister(Blocks.PLAYER_WALL_HEAD);
        insertAfter(registry, Blocks.ZOMBIE_HEAD, Blocks.PLAYER_WALL_HEAD, "player_wall_head");
        registry.unregister(Blocks.CREEPER_WALL_HEAD);
        insertAfter(registry, Blocks.PLAYER_HEAD, Blocks.CREEPER_WALL_HEAD, "creeper_wall_head");
        registry.unregister(Blocks.DRAGON_WALL_HEAD);
        insertAfter(registry, Blocks.CREEPER_HEAD, Blocks.DRAGON_WALL_HEAD, "dragon_wall_head");

        registry.unregister(Blocks.DEAD_TUBE_CORAL_FAN);
        registry.unregister(Blocks.DEAD_BRAIN_CORAL_FAN);
        registry.unregister(Blocks.DEAD_BUBBLE_CORAL_FAN);
        registry.unregister(Blocks.DEAD_FIRE_CORAL_FAN);
        registry.unregister(Blocks.DEAD_HORN_CORAL_FAN);
        registry.unregister(Blocks.TUBE_CORAL_FAN);
        registry.unregister(Blocks.BRAIN_CORAL_FAN);
        registry.unregister(Blocks.BUBBLE_CORAL_FAN);
        registry.unregister(Blocks.FIRE_CORAL_FAN);
        registry.unregister(Blocks.HORN_CORAL_FAN);
        insertAfter(registry, Blocks.HORN_CORAL_WALL_FAN, Blocks.DEAD_TUBE_CORAL_FAN, "dead_tube_coral_fan");
        insertAfter(registry, Blocks.DEAD_TUBE_CORAL_FAN, Blocks.DEAD_BRAIN_CORAL_FAN, "dead_brain_coral_fan");
        insertAfter(registry, Blocks.DEAD_BRAIN_CORAL_FAN, Blocks.DEAD_BUBBLE_CORAL_FAN, "dead_bubble_coral_fan");
        insertAfter(registry, Blocks.DEAD_BUBBLE_CORAL_FAN, Blocks.DEAD_FIRE_CORAL_FAN, "dead_fire_coral_fan");
        insertAfter(registry, Blocks.DEAD_FIRE_CORAL_FAN, Blocks.DEAD_HORN_CORAL_FAN, "dead_horn_coral_fan");
        insertAfter(registry, Blocks.DEAD_HORN_CORAL_FAN, Blocks.TUBE_CORAL_FAN, "tube_coral_fan");
        insertAfter(registry, Blocks.TUBE_CORAL_FAN, Blocks.BRAIN_CORAL_FAN, "brain_coral_fan");
        insertAfter(registry, Blocks.BRAIN_CORAL_FAN, Blocks.BUBBLE_CORAL_FAN, "bubble_coral_fan");
        insertAfter(registry, Blocks.BUBBLE_CORAL_FAN, Blocks.FIRE_CORAL_FAN, "fire_coral_fan");
        insertAfter(registry, Blocks.FIRE_CORAL_FAN, Blocks.HORN_CORAL_FAN, "horn_coral_fan");
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
        registry.unregister(Items.FLOWER_BANNER_PATTERN);
        registry.unregister(Items.CREEPER_BANNER_PATTERN);
        registry.unregister(Items.SKULL_BANNER_PATTERN);
        registry.unregister(Items.MOJANG_BANNER_PATTERN);
        registry.unregister(Items.GLOBE_BANNER_PATTERN);
        rename(registry, Items.OAK_SIGN, "sign");
        rename(registry, Items.RED_DYE, "rose_red");
        rename(registry, Items.GREEN_DYE, "cactus_green");
        rename(registry, Items.YELLOW_DYE, "dandelion_yellow");
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
        registry.unregister(ParticleTypes.FALLING_LAVA);
        registry.unregister(ParticleTypes.LANDING_LAVA);
        registry.unregister(ParticleTypes.FALLING_WATER);
        registry.unregister(ParticleTypes.FLASH);
        registry.unregister(ParticleTypes.COMPOSTER);
        registry.unregister(ParticleTypes.CAMPFIRE_COSY_SMOKE);
        registry.unregister(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE);
        registry.unregister(ParticleTypes.SNEEZE);
        registry.unregister(ParticleTypes.SQUID_INK);
        registry.unregister(ParticleTypes.BUBBLE_COLUMN_UP);

        insertAfter(registry, ParticleTypes.BUBBLE, ParticleTypes.BUBBLE_COLUMN_UP, "bubble_column_up");
        insertAfter(registry, ParticleTypes.CURRENT_DOWN, ParticleTypes.SQUID_INK, "squid_ink");
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

    private void modifySoundEventRegistry(ISimpleRegistry<SoundEvent> registry) {
        registry.unregister(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN);
        registry.unregister(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND);
        registry.unregister(SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA);
        registry.unregister(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC);
        registry.unregister(SoundEvents.ITEM_ARMOR_EQUIP_GOLD);
        registry.unregister(SoundEvents.ITEM_ARMOR_EQUIP_IRON);
        registry.unregister(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER);
        registry.unregister(SoundEvents.ITEM_ARMOR_EQUIP_TURTLE);
        registry.unregister(SoundEvents.ITEM_AXE_STRIP);
        registry.unregister(SoundEvents.BLOCK_BAMBOO_BREAK);
        registry.unregister(SoundEvents.BLOCK_BAMBOO_FALL);
        registry.unregister(SoundEvents.BLOCK_BAMBOO_HIT);
        registry.unregister(SoundEvents.BLOCK_BAMBOO_PLACE);
        registry.unregister(SoundEvents.BLOCK_BAMBOO_STEP);
        registry.unregister(SoundEvents.BLOCK_BAMBOO_SAPLING_BREAK);
        registry.unregister(SoundEvents.BLOCK_BAMBOO_SAPLING_HIT);
        registry.unregister(SoundEvents.BLOCK_BAMBOO_SAPLING_PLACE);
        registry.unregister(SoundEvents.BLOCK_BARREL_CLOSE);
        registry.unregister(SoundEvents.BLOCK_BARREL_OPEN);
        registry.unregister(SoundEvents.BLOCK_BEACON_ACTIVATE);
        registry.unregister(SoundEvents.BLOCK_BEACON_AMBIENT);
        registry.unregister(SoundEvents.BLOCK_BEACON_DEACTIVATE);
        registry.unregister(SoundEvents.BLOCK_BEACON_POWER_SELECT);
        registry.unregister(SoundEvents.BLOCK_BELL_USE);
        registry.unregister(SoundEvents.BLOCK_BELL_RESONATE);
        registry.unregister(SoundEvents.ITEM_BOOK_PAGE_TURN);
        registry.unregister(SoundEvents.ITEM_BOOK_PUT);
        registry.unregister(SoundEvents.BLOCK_BLASTFURNACE_FIRE_CRACKLE);
        registry.unregister(SoundEvents.ITEM_BOTTLE_EMPTY);
        registry.unregister(SoundEvents.ITEM_BOTTLE_FILL);
        registry.unregister(SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH);
        registry.unregister(SoundEvents.BLOCK_BREWING_STAND_BREW);
        registry.unregister(SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP);
        registry.unregister(SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT);
        registry.unregister(SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE);
        registry.unregister(SoundEvents.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT);
        registry.unregister(SoundEvents.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE);
        registry.unregister(SoundEvents.ITEM_BUCKET_EMPTY);
        registry.unregister(SoundEvents.ITEM_BUCKET_EMPTY_FISH);
        registry.unregister(SoundEvents.ITEM_BUCKET_EMPTY_LAVA);
        registry.unregister(SoundEvents.ITEM_BUCKET_FILL);
        registry.unregister(SoundEvents.ITEM_BUCKET_FILL_FISH);
        registry.unregister(SoundEvents.ITEM_BUCKET_FILL_LAVA);
        registry.unregister(SoundEvents.BLOCK_CAMPFIRE_CRACKLE);
        registry.unregister(SoundEvents.ENTITY_CAT_STRAY_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_CAT_EAT);
        registry.unregister(SoundEvents.ENTITY_CAT_BEG_FOR_FOOD);
        registry.unregister(SoundEvents.BLOCK_CHEST_CLOSE);
        registry.unregister(SoundEvents.BLOCK_CHEST_LOCKED);
        registry.unregister(SoundEvents.BLOCK_CHEST_OPEN);
        registry.unregister(SoundEvents.BLOCK_CHORUS_FLOWER_DEATH);
        registry.unregister(SoundEvents.BLOCK_CHORUS_FLOWER_GROW);
        registry.unregister(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT);
        registry.unregister(SoundEvents.BLOCK_WOOL_BREAK);
        registry.unregister(SoundEvents.BLOCK_WOOL_FALL);
        registry.unregister(SoundEvents.BLOCK_WOOL_HIT);
        registry.unregister(SoundEvents.BLOCK_WOOL_PLACE);
        registry.unregister(SoundEvents.BLOCK_WOOL_STEP);
        registry.unregister(SoundEvents.BLOCK_COMPARATOR_CLICK);
        registry.unregister(SoundEvents.BLOCK_COMPOSTER_EMPTY);
        registry.unregister(SoundEvents.BLOCK_COMPOSTER_FILL);
        registry.unregister(SoundEvents.BLOCK_COMPOSTER_FILL_SUCCESS);
        registry.unregister(SoundEvents.BLOCK_COMPOSTER_READY);
        registry.unregister(SoundEvents.BLOCK_CONDUIT_ACTIVATE);
        registry.unregister(SoundEvents.BLOCK_CONDUIT_AMBIENT);
        registry.unregister(SoundEvents.BLOCK_CONDUIT_AMBIENT_SHORT);
        registry.unregister(SoundEvents.BLOCK_CONDUIT_ATTACK_TARGET);
        registry.unregister(SoundEvents.BLOCK_CONDUIT_DEACTIVATE);
        registry.unregister(SoundEvents.BLOCK_CROP_BREAK);
        registry.unregister(SoundEvents.ITEM_CROP_PLANT);
        registry.unregister(SoundEvents.ITEM_CROSSBOW_HIT);
        registry.unregister(SoundEvents.ITEM_CROSSBOW_LOADING_END);
        registry.unregister(SoundEvents.ITEM_CROSSBOW_LOADING_MIDDLE);
        registry.unregister(SoundEvents.ITEM_CROSSBOW_LOADING_START);
        registry.unregister(SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_1);
        registry.unregister(SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_2);
        registry.unregister(SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_3);
        registry.unregister(SoundEvents.ITEM_CROSSBOW_SHOOT);
        registry.unregister(SoundEvents.BLOCK_DISPENSER_DISPENSE);
        registry.unregister(SoundEvents.BLOCK_DISPENSER_FAIL);
        registry.unregister(SoundEvents.BLOCK_DISPENSER_LAUNCH);
        registry.unregister(SoundEvents.ITEM_ELYTRA_FLYING);
        registry.unregister(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE);
        registry.unregister(SoundEvents.BLOCK_ENDER_CHEST_CLOSE);
        registry.unregister(SoundEvents.BLOCK_ENDER_CHEST_OPEN);
        registry.unregister(SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE);
        registry.unregister(SoundEvents.BLOCK_END_GATEWAY_SPAWN);
        registry.unregister(SoundEvents.BLOCK_END_PORTAL_FRAME_FILL);
        registry.unregister(SoundEvents.BLOCK_END_PORTAL_SPAWN);
        registry.unregister(SoundEvents.ENTITY_EVOKER_CELEBRATE);
        registry.unregister(SoundEvents.ENTITY_EVOKER_FANGS_ATTACK);
        registry.unregister(SoundEvents.BLOCK_FENCE_GATE_CLOSE);
        registry.unregister(SoundEvents.BLOCK_FENCE_GATE_OPEN);
        registry.unregister(SoundEvents.ITEM_FIRECHARGE_USE);
        registry.unregister(SoundEvents.BLOCK_FIRE_AMBIENT);
        registry.unregister(SoundEvents.BLOCK_FIRE_EXTINGUISH);
        registry.unregister(SoundEvents.ITEM_FLINTANDSTEEL_USE);
        registry.unregister(SoundEvents.ENTITY_FOX_AGGRO);
        registry.unregister(SoundEvents.ENTITY_FOX_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_FOX_BITE);
        registry.unregister(SoundEvents.ENTITY_FOX_DEATH);
        registry.unregister(SoundEvents.ENTITY_FOX_EAT);
        registry.unregister(SoundEvents.ENTITY_FOX_HURT);
        registry.unregister(SoundEvents.ENTITY_FOX_SCREECH);
        registry.unregister(SoundEvents.ENTITY_FOX_SLEEP);
        registry.unregister(SoundEvents.ENTITY_FOX_SNIFF);
        registry.unregister(SoundEvents.ENTITY_FOX_SPIT);
        registry.unregister(SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE);
        registry.unregister(SoundEvents.BLOCK_GLASS_BREAK);
        registry.unregister(SoundEvents.BLOCK_GLASS_FALL);
        registry.unregister(SoundEvents.BLOCK_GLASS_HIT);
        registry.unregister(SoundEvents.BLOCK_GLASS_PLACE);
        registry.unregister(SoundEvents.BLOCK_GLASS_STEP);
        registry.unregister(SoundEvents.BLOCK_GRASS_BREAK);
        registry.unregister(SoundEvents.BLOCK_GRASS_FALL);
        registry.unregister(SoundEvents.BLOCK_GRASS_HIT);
        registry.unregister(SoundEvents.BLOCK_GRASS_PLACE);
        registry.unregister(SoundEvents.BLOCK_GRASS_STEP);
        registry.unregister(SoundEvents.BLOCK_WET_GRASS_BREAK);
        registry.unregister(SoundEvents.BLOCK_WET_GRASS_FALL);
        registry.unregister(SoundEvents.BLOCK_WET_GRASS_HIT);
        registry.unregister(SoundEvents.BLOCK_WET_GRASS_PLACE);
        registry.unregister(SoundEvents.BLOCK_WET_GRASS_STEP);
        registry.unregister(SoundEvents.BLOCK_CORAL_BLOCK_BREAK);
        registry.unregister(SoundEvents.BLOCK_CORAL_BLOCK_FALL);
        registry.unregister(SoundEvents.BLOCK_CORAL_BLOCK_HIT);
        registry.unregister(SoundEvents.BLOCK_CORAL_BLOCK_PLACE);
        registry.unregister(SoundEvents.BLOCK_CORAL_BLOCK_STEP);
        registry.unregister(SoundEvents.BLOCK_GRAVEL_BREAK);
        registry.unregister(SoundEvents.BLOCK_GRAVEL_FALL);
        registry.unregister(SoundEvents.BLOCK_GRAVEL_HIT);
        registry.unregister(SoundEvents.BLOCK_GRAVEL_PLACE);
        registry.unregister(SoundEvents.BLOCK_GRAVEL_STEP);
        registry.unregister(SoundEvents.BLOCK_GRINDSTONE_USE);
        registry.unregister(SoundEvents.ITEM_HOE_TILL);
        registry.unregister(SoundEvents.ENTITY_RAVAGER_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_RAVAGER_ATTACK);
        registry.unregister(SoundEvents.ENTITY_RAVAGER_CELEBRATE);
        registry.unregister(SoundEvents.ENTITY_RAVAGER_DEATH);
        registry.unregister(SoundEvents.ENTITY_RAVAGER_HURT);
        registry.unregister(SoundEvents.ENTITY_RAVAGER_STEP);
        registry.unregister(SoundEvents.ENTITY_RAVAGER_STUNNED);
        registry.unregister(SoundEvents.ENTITY_RAVAGER_ROAR);
        registry.unregister(SoundEvents.BLOCK_IRON_DOOR_CLOSE);
        registry.unregister(SoundEvents.BLOCK_IRON_DOOR_OPEN);
        registry.unregister(SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE);
        registry.unregister(SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN);
        registry.unregister(SoundEvents.ENTITY_ITEM_BREAK);
        registry.unregister(SoundEvents.ENTITY_ITEM_PICKUP);
        registry.unregister(SoundEvents.BLOCK_LADDER_BREAK);
        registry.unregister(SoundEvents.BLOCK_LADDER_FALL);
        registry.unregister(SoundEvents.BLOCK_LADDER_HIT);
        registry.unregister(SoundEvents.BLOCK_LADDER_PLACE);
        registry.unregister(SoundEvents.BLOCK_LADDER_STEP);
        registry.unregister(SoundEvents.BLOCK_LANTERN_BREAK);
        registry.unregister(SoundEvents.BLOCK_LANTERN_FALL);
        registry.unregister(SoundEvents.BLOCK_LANTERN_HIT);
        registry.unregister(SoundEvents.BLOCK_LANTERN_PLACE);
        registry.unregister(SoundEvents.BLOCK_LANTERN_STEP);
        registry.unregister(SoundEvents.BLOCK_LAVA_AMBIENT);
        registry.unregister(SoundEvents.BLOCK_LAVA_EXTINGUISH);
        registry.unregister(SoundEvents.BLOCK_LAVA_POP);
        registry.unregister(SoundEvents.BLOCK_LEVER_CLICK);
        registry.unregister(SoundEvents.BLOCK_METAL_BREAK);
        registry.unregister(SoundEvents.BLOCK_METAL_FALL);
        registry.unregister(SoundEvents.BLOCK_METAL_HIT);
        registry.unregister(SoundEvents.BLOCK_METAL_PLACE);
        registry.unregister(SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF);
        registry.unregister(SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON);
        registry.unregister(SoundEvents.BLOCK_METAL_STEP);
        registry.unregister(SoundEvents.ENTITY_MOOSHROOM_CONVERT);
        registry.unregister(SoundEvents.ENTITY_MOOSHROOM_EAT);
        registry.unregister(SoundEvents.ENTITY_MOOSHROOM_MILK);
        registry.unregister(SoundEvents.ENTITY_MOOSHROOM_SUSPICIOUS_MILK);
        registry.unregister(SoundEvents.MUSIC_CREATIVE);
        registry.unregister(SoundEvents.MUSIC_CREDITS);
        registry.unregister(SoundEvents.MUSIC_DRAGON);
        registry.unregister(SoundEvents.MUSIC_END);
        registry.unregister(SoundEvents.MUSIC_GAME);
        registry.unregister(SoundEvents.MUSIC_MENU);
        registry.unregister(SoundEvents.MUSIC_NETHER);
        registry.unregister(SoundEvents.MUSIC_UNDER_WATER);
        registry.unregister(SoundEvents.BLOCK_NETHER_WART_BREAK);
        registry.unregister(SoundEvents.ITEM_NETHER_WART_PLANT);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_BASS);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_BELL);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_CHIME);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_GUITAR);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_HARP);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_HAT);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_PLING);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_SNARE);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_BIT);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_BANJO);
        registry.unregister(SoundEvents.ENTITY_OCELOT_HURT);
        registry.unregister(SoundEvents.ENTITY_OCELOT_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_OCELOT_DEATH);
        registry.unregister(SoundEvents.ENTITY_PANDA_PRE_SNEEZE);
        registry.unregister(SoundEvents.ENTITY_PANDA_SNEEZE);
        registry.unregister(SoundEvents.ENTITY_PANDA_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_PANDA_DEATH);
        registry.unregister(SoundEvents.ENTITY_PANDA_EAT);
        registry.unregister(SoundEvents.ENTITY_PANDA_STEP);
        registry.unregister(SoundEvents.ENTITY_PANDA_CANT_BREED);
        registry.unregister(SoundEvents.ENTITY_PANDA_AGGRESSIVE_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_PANDA_WORRIED_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_PANDA_HURT);
        registry.unregister(SoundEvents.ENTITY_PANDA_BITE);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_GUARDIAN);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_PANDA);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_PILLAGER);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_RAVAGER);
        registry.unregister(SoundEvents.ENTITY_PILLAGER_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_PILLAGER_CELEBRATE);
        registry.unregister(SoundEvents.ENTITY_PILLAGER_DEATH);
        registry.unregister(SoundEvents.ENTITY_PILLAGER_HURT);
        registry.unregister(SoundEvents.BLOCK_PISTON_CONTRACT);
        registry.unregister(SoundEvents.BLOCK_PISTON_EXTEND);
        registry.unregister(SoundEvents.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH);
        registry.unregister(SoundEvents.BLOCK_PORTAL_AMBIENT);
        registry.unregister(SoundEvents.BLOCK_PORTAL_TRAVEL);
        registry.unregister(SoundEvents.BLOCK_PORTAL_TRIGGER);
        registry.unregister(SoundEvents.BLOCK_PUMPKIN_CARVE);
        registry.unregister(SoundEvents.EVENT_RAID_HORN);
        registry.unregister(SoundEvents.MUSIC_DISC_11);
        registry.unregister(SoundEvents.MUSIC_DISC_13);
        registry.unregister(SoundEvents.MUSIC_DISC_BLOCKS);
        registry.unregister(SoundEvents.MUSIC_DISC_CAT);
        registry.unregister(SoundEvents.MUSIC_DISC_CHIRP);
        registry.unregister(SoundEvents.MUSIC_DISC_FAR);
        registry.unregister(SoundEvents.MUSIC_DISC_MALL);
        registry.unregister(SoundEvents.MUSIC_DISC_MELLOHI);
        registry.unregister(SoundEvents.MUSIC_DISC_STAL);
        registry.unregister(SoundEvents.MUSIC_DISC_STRAD);
        registry.unregister(SoundEvents.MUSIC_DISC_WAIT);
        registry.unregister(SoundEvents.MUSIC_DISC_WARD);
        registry.unregister(SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT);
        registry.unregister(SoundEvents.BLOCK_SAND_BREAK);
        registry.unregister(SoundEvents.BLOCK_SAND_FALL);
        registry.unregister(SoundEvents.BLOCK_SAND_HIT);
        registry.unregister(SoundEvents.BLOCK_SAND_PLACE);
        registry.unregister(SoundEvents.BLOCK_SAND_STEP);
        registry.unregister(SoundEvents.BLOCK_SCAFFOLDING_BREAK);
        registry.unregister(SoundEvents.BLOCK_SCAFFOLDING_FALL);
        registry.unregister(SoundEvents.BLOCK_SCAFFOLDING_HIT);
        registry.unregister(SoundEvents.BLOCK_SCAFFOLDING_PLACE);
        registry.unregister(SoundEvents.BLOCK_SCAFFOLDING_STEP);
        registry.unregister(SoundEvents.ITEM_SHIELD_BLOCK);
        registry.unregister(SoundEvents.ITEM_SHIELD_BREAK);
        registry.unregister(SoundEvents.ITEM_SHOVEL_FLATTEN);
        registry.unregister(SoundEvents.BLOCK_SHULKER_BOX_CLOSE);
        registry.unregister(SoundEvents.BLOCK_SHULKER_BOX_OPEN);
        registry.unregister(SoundEvents.ENTITY_SHULKER_BULLET_HIT);
        registry.unregister(SoundEvents.ENTITY_SHULKER_BULLET_HURT);
        registry.unregister(SoundEvents.ENTITY_SKELETON_HURT);
        registry.unregister(SoundEvents.ENTITY_SKELETON_SHOOT);
        registry.unregister(SoundEvents.ENTITY_SKELETON_STEP);
        registry.unregister(SoundEvents.BLOCK_SLIME_BLOCK_BREAK);
        registry.unregister(SoundEvents.BLOCK_SLIME_BLOCK_FALL);
        registry.unregister(SoundEvents.BLOCK_SLIME_BLOCK_HIT);
        registry.unregister(SoundEvents.BLOCK_SLIME_BLOCK_PLACE);
        registry.unregister(SoundEvents.BLOCK_SLIME_BLOCK_STEP);
        registry.unregister(SoundEvents.BLOCK_SMOKER_SMOKE);
        registry.unregister(SoundEvents.ENTITY_SNOWBALL_THROW);
        registry.unregister(SoundEvents.BLOCK_SNOW_BREAK);
        registry.unregister(SoundEvents.BLOCK_SNOW_FALL);
        registry.unregister(SoundEvents.BLOCK_SNOW_HIT);
        registry.unregister(SoundEvents.BLOCK_SNOW_PLACE);
        registry.unregister(SoundEvents.BLOCK_SNOW_STEP);
        registry.unregister(SoundEvents.BLOCK_STONE_BREAK);
        registry.unregister(SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF);
        registry.unregister(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON);
        registry.unregister(SoundEvents.BLOCK_STONE_FALL);
        registry.unregister(SoundEvents.BLOCK_STONE_HIT);
        registry.unregister(SoundEvents.BLOCK_STONE_PLACE);
        registry.unregister(SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_OFF);
        registry.unregister(SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON);
        registry.unregister(SoundEvents.BLOCK_STONE_STEP);
        registry.unregister(SoundEvents.BLOCK_SWEET_BERRY_BUSH_BREAK);
        registry.unregister(SoundEvents.BLOCK_SWEET_BERRY_BUSH_PLACE);
        registry.unregister(SoundEvents.ITEM_SWEET_BERRIES_PICK_FROM_BUSH);
        registry.unregister(SoundEvents.ENCHANT_THORNS_HIT);
        registry.unregister(SoundEvents.ITEM_TOTEM_USE);
        registry.unregister(SoundEvents.ITEM_TRIDENT_HIT);
        registry.unregister(SoundEvents.ITEM_TRIDENT_HIT_GROUND);
        registry.unregister(SoundEvents.ITEM_TRIDENT_RETURN);
        registry.unregister(SoundEvents.ITEM_TRIDENT_RIPTIDE_1);
        registry.unregister(SoundEvents.ITEM_TRIDENT_RIPTIDE_2);
        registry.unregister(SoundEvents.ITEM_TRIDENT_RIPTIDE_3);
        registry.unregister(SoundEvents.ITEM_TRIDENT_THROW);
        registry.unregister(SoundEvents.ITEM_TRIDENT_THUNDER);
        registry.unregister(SoundEvents.BLOCK_TRIPWIRE_ATTACH);
        registry.unregister(SoundEvents.BLOCK_TRIPWIRE_CLICK_OFF);
        registry.unregister(SoundEvents.BLOCK_TRIPWIRE_CLICK_ON);
        registry.unregister(SoundEvents.BLOCK_TRIPWIRE_DETACH);
        registry.unregister(SoundEvents.UI_BUTTON_CLICK);
        registry.unregister(SoundEvents.UI_LOOM_SELECT_PATTERN);
        registry.unregister(SoundEvents.UI_LOOM_TAKE_RESULT);
        registry.unregister(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT);
        registry.unregister(SoundEvents.UI_STONECUTTER_TAKE_RESULT);
        registry.unregister(SoundEvents.UI_STONECUTTER_SELECT_RECIPE);
        registry.unregister(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
        registry.unregister(SoundEvents.UI_TOAST_IN);
        registry.unregister(SoundEvents.UI_TOAST_OUT);
        registry.unregister(SoundEvents.ENTITY_VILLAGER_CELEBRATE);
        registry.unregister(SoundEvents.ENTITY_VILLAGER_WORK_ARMORER);
        registry.unregister(SoundEvents.ENTITY_VILLAGER_WORK_BUTCHER);
        registry.unregister(SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER);
        registry.unregister(SoundEvents.ENTITY_VILLAGER_WORK_CLERIC);
        registry.unregister(SoundEvents.ENTITY_VILLAGER_WORK_FARMER);
        registry.unregister(SoundEvents.ENTITY_VILLAGER_WORK_FISHERMAN);
        registry.unregister(SoundEvents.ENTITY_VILLAGER_WORK_FLETCHER);
        registry.unregister(SoundEvents.ENTITY_VILLAGER_WORK_LEATHERWORKER);
        registry.unregister(SoundEvents.ENTITY_VILLAGER_WORK_LIBRARIAN);
        registry.unregister(SoundEvents.ENTITY_VILLAGER_WORK_MASON);
        registry.unregister(SoundEvents.ENTITY_VILLAGER_WORK_SHEPHERD);
        registry.unregister(SoundEvents.ENTITY_VILLAGER_WORK_TOOLSMITH);
        registry.unregister(SoundEvents.ENTITY_VILLAGER_WORK_WEAPONSMITH);
        registry.unregister(SoundEvents.ENTITY_VINDICATOR_CELEBRATE);
        registry.unregister(SoundEvents.BLOCK_LILY_PAD_PLACE);
        registry.unregister(SoundEvents.ENTITY_WANDERING_TRADER_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_WANDERING_TRADER_DEATH);
        registry.unregister(SoundEvents.ENTITY_WANDERING_TRADER_DISAPPEARED);
        registry.unregister(SoundEvents.ENTITY_WANDERING_TRADER_DRINK_MILK);
        registry.unregister(SoundEvents.ENTITY_WANDERING_TRADER_DRINK_POTION);
        registry.unregister(SoundEvents.ENTITY_WANDERING_TRADER_HURT);
        registry.unregister(SoundEvents.ENTITY_WANDERING_TRADER_NO);
        registry.unregister(SoundEvents.ENTITY_WANDERING_TRADER_REAPPEARED);
        registry.unregister(SoundEvents.ENTITY_WANDERING_TRADER_TRADE);
        registry.unregister(SoundEvents.ENTITY_WANDERING_TRADER_YES);
        registry.unregister(SoundEvents.BLOCK_WATER_AMBIENT);
        registry.unregister(SoundEvents.WEATHER_RAIN);
        registry.unregister(SoundEvents.WEATHER_RAIN_ABOVE);
        registry.unregister(SoundEvents.ENTITY_WITCH_CELEBRATE);
        registry.unregister(SoundEvents.ENTITY_WITHER_SPAWN);
        registry.unregister(SoundEvents.BLOCK_WOODEN_DOOR_CLOSE);
        registry.unregister(SoundEvents.BLOCK_WOODEN_DOOR_OPEN);
        registry.unregister(SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE);
        registry.unregister(SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN);
        registry.unregister(SoundEvents.BLOCK_WOOD_BREAK);
        registry.unregister(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_OFF);
        registry.unregister(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON);
        registry.unregister(SoundEvents.BLOCK_WOOD_FALL);
        registry.unregister(SoundEvents.BLOCK_WOOD_HIT);
        registry.unregister(SoundEvents.BLOCK_WOOD_PLACE);
        registry.unregister(SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_OFF);
        registry.unregister(SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON);
        registry.unregister(SoundEvents.BLOCK_WOOD_STEP);
        registry.unregister(SoundEvents.ENTITY_ZOMBIE_HURT);
        registry.unregister(SoundEvents.ENTITY_ZOMBIE_INFECT);
        registry.unregister(SoundEvents.ENTITY_ZOMBIE_STEP);
        registry.unregister(SoundEvents.ENTITY_ZOMBIE_VILLAGER_STEP);

        insertAfter(registry, SoundEvents.BLOCK_ANVIL_USE, SoundEvents.BLOCK_BEACON_ACTIVATE, "block.beacon.activate");
        insertAfter(registry, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundEvents.BLOCK_BEACON_AMBIENT, "block.beacon.ambient");
        insertAfter(registry, SoundEvents.BLOCK_BEACON_AMBIENT, SoundEvents.BLOCK_BEACON_DEACTIVATE, "block.beacon.deactivate");
        insertAfter(registry, SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundEvents.BLOCK_BEACON_POWER_SELECT, "block.beacon.power_select");
        insertAfter(registry, SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundEvents.BLOCK_BREWING_STAND_BREW, "block.brewing_stand.brew");
        insertAfter(registry, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, "block.bubble_column.bubble_pop");
        insertAfter(registry, SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, "block.bubble_column.upwards_ambient");
        insertAfter(registry, SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE, "block.bubble_column.upwards_inside");
        insertAfter(registry, SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE, SoundEvents.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, "block.bubble_column.whirlpool_ambient");
        insertAfter(registry, SoundEvents.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundEvents.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE, "block.bubble_column.whirlpool_inside");
        insertAfter(registry, SoundEvents.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE, SoundEvents.BLOCK_CHEST_CLOSE, "block.chest.close");
        insertAfter(registry, SoundEvents.BLOCK_CHEST_CLOSE, SoundEvents.BLOCK_CHEST_LOCKED, "block.chest.locked");
        insertAfter(registry, SoundEvents.BLOCK_CHEST_LOCKED, SoundEvents.BLOCK_CHEST_OPEN, "block.chest.open");
        insertAfter(registry, SoundEvents.BLOCK_CHEST_OPEN, SoundEvents.BLOCK_CHORUS_FLOWER_DEATH, "block.chorus_flower.death");
        insertAfter(registry, SoundEvents.BLOCK_CHORUS_FLOWER_DEATH, SoundEvents.BLOCK_CHORUS_FLOWER_GROW, "block.chorus_flower.grow");
        insertAfter(registry, SoundEvents.BLOCK_CHORUS_FLOWER_GROW, SoundEvents.BLOCK_WOOL_BREAK, "block.wool.break");
        insertAfter(registry, SoundEvents.BLOCK_WOOL_BREAK, SoundEvents.BLOCK_WOOL_FALL, "block.wool.fall");
        insertAfter(registry, SoundEvents.BLOCK_WOOL_FALL, SoundEvents.BLOCK_WOOL_HIT, "block.wool.hit");
        insertAfter(registry, SoundEvents.BLOCK_WOOL_HIT, SoundEvents.BLOCK_WOOL_PLACE, "block.wool.place");
        insertAfter(registry, SoundEvents.BLOCK_WOOL_PLACE, SoundEvents.BLOCK_WOOL_STEP, "block.wool.step");
        insertAfter(registry, SoundEvents.BLOCK_WOOL_STEP, SoundEvents.BLOCK_COMPARATOR_CLICK, "block.comparator.click");
        insertAfter(registry, SoundEvents.BLOCK_COMPARATOR_CLICK, SoundEvents.BLOCK_CONDUIT_ACTIVATE, "block.conduit.activate");
        insertAfter(registry, SoundEvents.BLOCK_CONDUIT_ACTIVATE, SoundEvents.BLOCK_CONDUIT_AMBIENT, "block.conduit.ambient");
        insertAfter(registry, SoundEvents.BLOCK_CONDUIT_AMBIENT, SoundEvents.BLOCK_CONDUIT_AMBIENT_SHORT, "block.conduit.ambient.short");
        insertAfter(registry, SoundEvents.BLOCK_CONDUIT_AMBIENT_SHORT, SoundEvents.BLOCK_CONDUIT_ATTACK_TARGET, "block.conduit.attack.target");
        insertAfter(registry, SoundEvents.BLOCK_CONDUIT_ATTACK_TARGET, SoundEvents.BLOCK_CONDUIT_DEACTIVATE, "block.conduit.deactivate");
        insertAfter(registry, SoundEvents.BLOCK_CONDUIT_DEACTIVATE, SoundEvents.BLOCK_DISPENSER_DISPENSE, "block.dispenser.dispense");
        insertAfter(registry, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundEvents.BLOCK_DISPENSER_FAIL, "block.dispenser.fail");
        insertAfter(registry, SoundEvents.BLOCK_DISPENSER_FAIL, SoundEvents.BLOCK_DISPENSER_LAUNCH, "block.dispenser.launch");
        insertAfter(registry, SoundEvents.BLOCK_DISPENSER_LAUNCH, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, "block.enchantment_table.use");
        insertAfter(registry, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundEvents.BLOCK_END_GATEWAY_SPAWN, "block.end_gateway.spawn");
        insertAfter(registry, SoundEvents.BLOCK_END_GATEWAY_SPAWN, SoundEvents.BLOCK_END_PORTAL_SPAWN, "block.end_portal.spawn");
        insertAfter(registry, SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, "block.end_portal_frame.fill");
        insertAfter(registry, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundEvents.BLOCK_ENDER_CHEST_CLOSE, "block.ender_chest.close");
        insertAfter(registry, SoundEvents.BLOCK_ENDER_CHEST_CLOSE, SoundEvents.BLOCK_ENDER_CHEST_OPEN, "block.ender_chest.open");
        insertAfter(registry, SoundEvents.BLOCK_ENDER_CHEST_OPEN, SoundEvents.BLOCK_FENCE_GATE_CLOSE, "block.fence_gate.close");
        insertAfter(registry, SoundEvents.BLOCK_FENCE_GATE_CLOSE, SoundEvents.BLOCK_FENCE_GATE_OPEN, "block.fence_gate.open");
        insertAfter(registry, SoundEvents.BLOCK_FENCE_GATE_OPEN, SoundEvents.BLOCK_FIRE_AMBIENT, "block.fire.ambient");
        insertAfter(registry, SoundEvents.BLOCK_FIRE_AMBIENT, SoundEvents.BLOCK_FIRE_EXTINGUISH, "block.fire.extinguish");
        insertAfter(registry, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, "block.furnace.fire_crackle");
        insertAfter(registry, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundEvents.BLOCK_GLASS_BREAK, "block.glass.break");
        insertAfter(registry, SoundEvents.BLOCK_GLASS_BREAK, SoundEvents.BLOCK_GLASS_FALL, "block.glass.fall");
        insertAfter(registry, SoundEvents.BLOCK_GLASS_FALL, SoundEvents.BLOCK_GLASS_HIT, "block.glass.hit");
        insertAfter(registry, SoundEvents.BLOCK_GLASS_HIT, SoundEvents.BLOCK_GLASS_PLACE, "block.glass.place");
        insertAfter(registry, SoundEvents.BLOCK_GLASS_PLACE, SoundEvents.BLOCK_GLASS_STEP, "block.glass.step");
        insertAfter(registry, SoundEvents.BLOCK_GLASS_STEP, SoundEvents.BLOCK_GRASS_BREAK, "block.grass.break");
        insertAfter(registry, SoundEvents.BLOCK_GRASS_BREAK, SoundEvents.BLOCK_GRASS_FALL, "block.grass.fall");
        insertAfter(registry, SoundEvents.BLOCK_GRASS_FALL, SoundEvents.BLOCK_GRASS_HIT, "block.grass.hit");
        insertAfter(registry, SoundEvents.BLOCK_GRASS_HIT, SoundEvents.BLOCK_GRASS_PLACE, "block.grass.place");
        insertAfter(registry, SoundEvents.BLOCK_GRASS_PLACE, SoundEvents.BLOCK_GRASS_STEP, "block.grass.step");
        insertAfter(registry, SoundEvents.BLOCK_GRASS_STEP, SoundEvents.BLOCK_WET_GRASS_BREAK, "block.wet_grass.break");
        insertAfter(registry, SoundEvents.BLOCK_WET_GRASS_BREAK, SoundEvents.BLOCK_WET_GRASS_FALL, "block.wet_grass.fall");
        insertAfter(registry, SoundEvents.BLOCK_WET_GRASS_FALL, SoundEvents.BLOCK_WET_GRASS_HIT, "block.wet_grass.hit");
        insertAfter(registry, SoundEvents.BLOCK_WET_GRASS_HIT, SoundEvents.BLOCK_WET_GRASS_PLACE, "block.wet_grass.place");
        insertAfter(registry, SoundEvents.BLOCK_WET_GRASS_PLACE, SoundEvents.BLOCK_WET_GRASS_STEP, "block.wet_grass.step");
        insertAfter(registry, SoundEvents.BLOCK_WET_GRASS_STEP, SoundEvents.BLOCK_CORAL_BLOCK_BREAK, "block.coral_block.break");
        insertAfter(registry, SoundEvents.BLOCK_CORAL_BLOCK_BREAK, SoundEvents.BLOCK_CORAL_BLOCK_FALL, "block.coral_block.fall");
        insertAfter(registry, SoundEvents.BLOCK_CORAL_BLOCK_FALL, SoundEvents.BLOCK_CORAL_BLOCK_HIT, "block.coral_block.hit");
        insertAfter(registry, SoundEvents.BLOCK_CORAL_BLOCK_HIT, SoundEvents.BLOCK_CORAL_BLOCK_PLACE, "block.coral_block.place");
        insertAfter(registry, SoundEvents.BLOCK_CORAL_BLOCK_PLACE, SoundEvents.BLOCK_CORAL_BLOCK_STEP, "block.coral_block.step");
        insertAfter(registry, SoundEvents.BLOCK_CORAL_BLOCK_STEP, SoundEvents.BLOCK_GRAVEL_BREAK, "block.gravel.break");
        insertAfter(registry, SoundEvents.BLOCK_GRAVEL_BREAK, SoundEvents.BLOCK_GRAVEL_FALL, "block.gravel.fall");
        insertAfter(registry, SoundEvents.BLOCK_GRAVEL_FALL, SoundEvents.BLOCK_GRAVEL_HIT, "block.gravel.hit");
        insertAfter(registry, SoundEvents.BLOCK_GRAVEL_HIT, SoundEvents.BLOCK_GRAVEL_PLACE, "block.gravel.place");
        insertAfter(registry, SoundEvents.BLOCK_GRAVEL_PLACE, SoundEvents.BLOCK_GRAVEL_STEP, "block.gravel.step");
        insertAfter(registry, SoundEvents.BLOCK_GRAVEL_STEP, SoundEvents.BLOCK_IRON_DOOR_CLOSE, "block.iron_door.close");
        insertAfter(registry, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundEvents.BLOCK_IRON_DOOR_OPEN, "block.iron_door.open");
        insertAfter(registry, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, "block.iron_trapdoor.close");
        insertAfter(registry, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, "block.iron_trapdoor.open");
        insertAfter(registry, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundEvents.BLOCK_LADDER_BREAK, "block.ladder.break");
        insertAfter(registry, SoundEvents.BLOCK_LADDER_BREAK, SoundEvents.BLOCK_LADDER_FALL, "block.ladder.fall");
        insertAfter(registry, SoundEvents.BLOCK_LADDER_FALL, SoundEvents.BLOCK_LADDER_HIT, "block.ladder.hit");
        insertAfter(registry, SoundEvents.BLOCK_LADDER_HIT, SoundEvents.BLOCK_LADDER_PLACE, "block.ladder.place");
        insertAfter(registry, SoundEvents.BLOCK_LADDER_PLACE, SoundEvents.BLOCK_LADDER_STEP, "block.ladder.step");
        insertAfter(registry, SoundEvents.BLOCK_LADDER_STEP, SoundEvents.BLOCK_LAVA_AMBIENT, "block.lava.ambient");
        insertAfter(registry, SoundEvents.BLOCK_LAVA_AMBIENT, SoundEvents.BLOCK_LAVA_EXTINGUISH, "block.lava.extinguish");
        insertAfter(registry, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundEvents.BLOCK_LAVA_POP, "block.lava.pop");
        insertAfter(registry, SoundEvents.BLOCK_LAVA_POP, SoundEvents.BLOCK_LEVER_CLICK, "block.lever.click");
        insertAfter(registry, SoundEvents.BLOCK_LEVER_CLICK, SoundEvents.BLOCK_METAL_BREAK, "block.metal.break");
        insertAfter(registry, SoundEvents.BLOCK_METAL_BREAK, SoundEvents.BLOCK_METAL_FALL, "block.metal.fall");
        insertAfter(registry, SoundEvents.BLOCK_METAL_FALL, SoundEvents.BLOCK_METAL_HIT, "block.metal.hit");
        insertAfter(registry, SoundEvents.BLOCK_METAL_HIT, SoundEvents.BLOCK_METAL_PLACE, "block.metal.place");
        insertAfter(registry, SoundEvents.BLOCK_METAL_PLACE, SoundEvents.BLOCK_METAL_STEP, "block.metal.step");
        insertAfter(registry, SoundEvents.BLOCK_METAL_STEP, SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, "block.metal_pressure_plate.click_off");
        insertAfter(registry, SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, "block.metal_pressure_plate.click_on");
        insertAfter(registry, SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM, "block.note_block.basedrum");
        insertAfter(registry, SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM, SoundEvents.BLOCK_NOTE_BLOCK_BASS, "block.note_block.bass");
        insertAfter(registry, SoundEvents.BLOCK_NOTE_BLOCK_BASS, SoundEvents.BLOCK_NOTE_BLOCK_BELL, "block.note_block.bell");
        insertAfter(registry, SoundEvents.BLOCK_NOTE_BLOCK_BELL, SoundEvents.BLOCK_NOTE_BLOCK_CHIME, "block.note_block.chime");
        insertAfter(registry, SoundEvents.BLOCK_NOTE_BLOCK_CHIME, SoundEvents.BLOCK_NOTE_BLOCK_FLUTE, "block.note_block.flute");
        insertAfter(registry, SoundEvents.BLOCK_NOTE_BLOCK_FLUTE, SoundEvents.BLOCK_NOTE_BLOCK_GUITAR, "block.note_block.guitar");
        insertAfter(registry, SoundEvents.BLOCK_NOTE_BLOCK_GUITAR, SoundEvents.BLOCK_NOTE_BLOCK_HARP, "block.note_block.harp");
        insertAfter(registry, SoundEvents.BLOCK_NOTE_BLOCK_HARP, SoundEvents.BLOCK_NOTE_BLOCK_HAT, "block.note_block.hat");
        insertAfter(registry, SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundEvents.BLOCK_NOTE_BLOCK_PLING, "block.note_block.pling");
        insertAfter(registry, SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundEvents.BLOCK_NOTE_BLOCK_SNARE, "block.note_block.snare");
        insertAfter(registry, SoundEvents.BLOCK_NOTE_BLOCK_SNARE, SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE, "block.note_block.xylophone");
        insertAfter(registry, SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE, SoundEvents.BLOCK_PISTON_CONTRACT, "block.piston.contract");
        insertAfter(registry, SoundEvents.BLOCK_PISTON_CONTRACT, SoundEvents.BLOCK_PISTON_EXTEND, "block.piston.extend");
        insertAfter(registry, SoundEvents.BLOCK_PISTON_EXTEND, SoundEvents.BLOCK_PORTAL_AMBIENT, "block.portal.ambient");
        insertAfter(registry, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundEvents.BLOCK_PORTAL_TRAVEL, "block.portal.travel");
        insertAfter(registry, SoundEvents.BLOCK_PORTAL_TRAVEL, SoundEvents.BLOCK_PORTAL_TRIGGER, "block.portal.trigger");
        insertAfter(registry, SoundEvents.BLOCK_PORTAL_TRIGGER, SoundEvents.BLOCK_PUMPKIN_CARVE, "block.pumpkin.carve");
        insertAfter(registry, SoundEvents.BLOCK_PUMPKIN_CARVE, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, "block.redstone_torch.burnout");
        insertAfter(registry, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundEvents.BLOCK_SAND_BREAK, "block.sand.break");
        insertAfter(registry, SoundEvents.BLOCK_SAND_BREAK, SoundEvents.BLOCK_SAND_FALL, "block.sand.fall");
        insertAfter(registry, SoundEvents.BLOCK_SAND_FALL, SoundEvents.BLOCK_SAND_HIT, "block.sand.hit");
        insertAfter(registry, SoundEvents.BLOCK_SAND_HIT, SoundEvents.BLOCK_SAND_PLACE, "block.sand.place");
        insertAfter(registry, SoundEvents.BLOCK_SAND_PLACE, SoundEvents.BLOCK_SAND_STEP, "block.sand.step");
        insertAfter(registry, SoundEvents.BLOCK_SAND_STEP, SoundEvents.BLOCK_SHULKER_BOX_CLOSE, "block.shulker_box.close");
        insertAfter(registry, SoundEvents.BLOCK_SHULKER_BOX_CLOSE, SoundEvents.BLOCK_SHULKER_BOX_OPEN, "block.shulker_box.open");
        insertAfter(registry, SoundEvents.BLOCK_SHULKER_BOX_OPEN, SoundEvents.BLOCK_SLIME_BLOCK_BREAK, "block.slime_block.break");
        insertAfter(registry, SoundEvents.BLOCK_SLIME_BLOCK_BREAK, SoundEvents.BLOCK_SLIME_BLOCK_FALL, "block.slime_block.fall");
        insertAfter(registry, SoundEvents.BLOCK_SLIME_BLOCK_FALL, SoundEvents.BLOCK_SLIME_BLOCK_HIT, "block.slime_block.hit");
        insertAfter(registry, SoundEvents.BLOCK_SLIME_BLOCK_HIT, SoundEvents.BLOCK_SLIME_BLOCK_PLACE, "block.slime_block.place");
        insertAfter(registry, SoundEvents.BLOCK_SLIME_BLOCK_PLACE, SoundEvents.BLOCK_SLIME_BLOCK_STEP, "block.slime_block.step");
        insertAfter(registry, SoundEvents.BLOCK_SLIME_BLOCK_STEP, SoundEvents.BLOCK_SNOW_BREAK, "block.snow.break");
        insertAfter(registry, SoundEvents.BLOCK_SNOW_BREAK, SoundEvents.BLOCK_SNOW_FALL, "block.snow.fall");
        insertAfter(registry, SoundEvents.BLOCK_SNOW_FALL, SoundEvents.BLOCK_SNOW_HIT, "block.snow.hit");
        insertAfter(registry, SoundEvents.BLOCK_SNOW_HIT, SoundEvents.BLOCK_SNOW_PLACE, "block.snow.place");
        insertAfter(registry, SoundEvents.BLOCK_SNOW_PLACE, SoundEvents.BLOCK_SNOW_STEP, "block.snow.step");
        insertAfter(registry, SoundEvents.BLOCK_SNOW_STEP, SoundEvents.BLOCK_STONE_BREAK, "block.stone.break");
        insertAfter(registry, SoundEvents.BLOCK_STONE_BREAK, SoundEvents.BLOCK_STONE_FALL, "block.stone.fall");
        insertAfter(registry, SoundEvents.BLOCK_STONE_FALL, SoundEvents.BLOCK_STONE_HIT, "block.stone.hit");
        insertAfter(registry, SoundEvents.BLOCK_STONE_HIT, SoundEvents.BLOCK_STONE_PLACE, "block.stone.place");
        insertAfter(registry, SoundEvents.BLOCK_STONE_PLACE, SoundEvents.BLOCK_STONE_STEP, "block.stone.step");
        insertAfter(registry, SoundEvents.BLOCK_STONE_STEP, SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, "block.stone_button.click_off");
        insertAfter(registry, SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, "block.stone_button.click_on");
        insertAfter(registry, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_OFF, "block.stone_pressure_plate.click_off");
        insertAfter(registry, SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON, "block.stone_pressure_plate.click_on");
        insertAfter(registry, SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON, SoundEvents.BLOCK_TRIPWIRE_ATTACH, "block.tripwire.attach");
        insertAfter(registry, SoundEvents.BLOCK_TRIPWIRE_ATTACH, SoundEvents.BLOCK_TRIPWIRE_CLICK_OFF, "block.tripwire.click_off");
        insertAfter(registry, SoundEvents.BLOCK_TRIPWIRE_CLICK_OFF, SoundEvents.BLOCK_TRIPWIRE_CLICK_ON, "block.tripwire.click_on");
        insertAfter(registry, SoundEvents.BLOCK_TRIPWIRE_CLICK_ON, SoundEvents.BLOCK_TRIPWIRE_DETACH, "block.tripwire.detach");
        insertAfter(registry, SoundEvents.BLOCK_TRIPWIRE_DETACH, SoundEvents.BLOCK_WATER_AMBIENT, "block.water.ambient");
        insertAfter(registry, SoundEvents.BLOCK_WATER_AMBIENT, SoundEvents.BLOCK_LILY_PAD_PLACE, "block.lily_pad.place");
        insertAfter(registry, SoundEvents.BLOCK_LILY_PAD_PLACE, SoundEvents.BLOCK_WOOD_BREAK, "block.wood.break");
        insertAfter(registry, SoundEvents.BLOCK_WOOD_BREAK, SoundEvents.BLOCK_WOOD_FALL, "block.wood.fall");
        insertAfter(registry, SoundEvents.BLOCK_WOOD_FALL, SoundEvents.BLOCK_WOOD_HIT, "block.wood.hit");
        insertAfter(registry, SoundEvents.BLOCK_WOOD_HIT, SoundEvents.BLOCK_WOOD_PLACE, "block.wood.place");
        insertAfter(registry, SoundEvents.BLOCK_WOOD_PLACE, SoundEvents.BLOCK_WOOD_STEP, "block.wood.step");
        insertAfter(registry, SoundEvents.BLOCK_WOOD_STEP, SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_OFF, "block.wooden_button.click_off");
        insertAfter(registry, SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_OFF, SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, "block.wooden_button.click_on");
        insertAfter(registry, SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_OFF, "block.wooden_pressure_plate.click_off");
        insertAfter(registry, SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_OFF, SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON, "block.wooden_pressure_plate.click_on");
        insertAfter(registry, SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON, SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, "block.wooden_door.close");
        insertAfter(registry, SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, SoundEvents.BLOCK_WOODEN_DOOR_OPEN, "block.wooden_door.open");
        insertAfter(registry, SoundEvents.BLOCK_WOODEN_DOOR_OPEN, SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, "block.wooden_trapdoor.close");
        insertAfter(registry, SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, "block.wooden_trapdoor.open");
        insertAfter(registry, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundEvents.ENCHANT_THORNS_HIT, "enchant.thorns.hit");
        insertAfter(registry, SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, "entity.dragon_fireball.explode");
        insertAfter(registry, SoundEvents.ENTITY_EVOKER_PREPARE_WOLOLO, SoundEvents.ENTITY_EVOKER_FANGS_ATTACK, "entity.evoker_fangs.attack");
        insertAfter(registry, SoundEvents.ENTITY_IRON_GOLEM_STEP, SoundEvents.ENTITY_ITEM_BREAK, "entity.item.break");
        insertAfter(registry, SoundEvents.ENTITY_ITEM_BREAK, SoundEvents.ENTITY_ITEM_PICKUP, "entity.item.pickup");
        insertAfter(registry, SoundEvents.ENTITY_SHULKER_TELEPORT, SoundEvents.ENTITY_SHULKER_BULLET_HIT, "entity.shulker_bullet.hit");
        insertAfter(registry, SoundEvents.ENTITY_SHULKER_BULLET_HIT, SoundEvents.ENTITY_SHULKER_BULLET_HURT, "entity.shulker_bullet.hurt");
        insertAfter(registry, SoundEvents.ENTITY_SKELETON_DEATH, SoundEvents.ENTITY_SKELETON_HURT, "entity.skeleton.hurt");
        insertAfter(registry, SoundEvents.ENTITY_SKELETON_HURT, SoundEvents.ENTITY_SKELETON_SHOOT, "entity.skeleton.shoot");
        insertAfter(registry, SoundEvents.ENTITY_SKELETON_SHOOT, SoundEvents.ENTITY_SKELETON_STEP, "entity.skeleton.step");
        insertAfter(registry, SoundEvents.ENTITY_SNOW_GOLEM_SHOOT, SoundEvents.ENTITY_SNOWBALL_THROW, "entity.snowball.throw");
        insertAfter(registry, SoundEvents.ENTITY_WITHER_SHOOT, SoundEvents.ENTITY_WITHER_SPAWN, "entity.wither.spawn");
        insertAfter(registry, SoundEvents.ENTITY_ZOMBIE_DESTROY_EGG, SoundEvents.ENTITY_ZOMBIE_HURT, "entity.zombie.hurt");
        insertAfter(registry, SoundEvents.ENTITY_ZOMBIE_HURT, SoundEvents.ENTITY_ZOMBIE_INFECT, "entity.zombie.infect");
        insertAfter(registry, SoundEvents.ENTITY_ZOMBIE_INFECT, SoundEvents.ENTITY_ZOMBIE_STEP, "entity.zombie.step");
        insertAfter(registry, SoundEvents.ENTITY_ZOMBIE_VILLAGER_HURT, SoundEvents.ENTITY_ZOMBIE_VILLAGER_STEP, "entity.zombie_villager.step");
        insertAfter(registry, SoundEvents.ENTITY_ZOMBIE_VILLAGER_STEP, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, "item.armor.equip_chain");
        insertAfter(registry, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, "item.armor.equip_diamond");
        insertAfter(registry, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA, "item.armor.equip_elytra");
        insertAfter(registry, SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, "item.armor.equip_generic");
        insertAfter(registry, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundEvents.ITEM_ARMOR_EQUIP_GOLD, "item.armor.equip_gold");
        insertAfter(registry, SoundEvents.ITEM_ARMOR_EQUIP_GOLD, SoundEvents.ITEM_ARMOR_EQUIP_IRON, "item.armor.equip_iron");
        insertAfter(registry, SoundEvents.ITEM_ARMOR_EQUIP_IRON, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, "item.armor.equip_leather");
        insertAfter(registry, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundEvents.ITEM_ARMOR_EQUIP_TURTLE, "item.armor.equip_turtle");
        insertAfter(registry, SoundEvents.ITEM_ARMOR_EQUIP_TURTLE, SoundEvents.ITEM_AXE_STRIP, "item.axe.strip");
        insertAfter(registry, SoundEvents.ITEM_AXE_STRIP, SoundEvents.ITEM_BOTTLE_EMPTY, "item.bottle.empty");
        insertAfter(registry, SoundEvents.ITEM_BOTTLE_EMPTY, SoundEvents.ITEM_BOTTLE_FILL, "item.bottle.fill");
        insertAfter(registry, SoundEvents.ITEM_BOTTLE_FILL, SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH, "item.bottle.fill_dragonbreath");
        insertAfter(registry, SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH, SoundEvents.ITEM_BUCKET_EMPTY, "item.bucket.empty");
        insertAfter(registry, SoundEvents.ITEM_BUCKET_EMPTY, SoundEvents.ITEM_BUCKET_EMPTY_FISH, "item.bucket.empty_fish");
        insertAfter(registry, SoundEvents.ITEM_BUCKET_EMPTY_FISH, SoundEvents.ITEM_BUCKET_EMPTY_LAVA, "item.bucket.empty_lava");
        insertAfter(registry, SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundEvents.ITEM_BUCKET_FILL, "item.bucket.fill");
        insertAfter(registry, SoundEvents.ITEM_BUCKET_FILL, SoundEvents.ITEM_BUCKET_FILL_FISH, "item.bucket.fill_fish");
        insertAfter(registry, SoundEvents.ITEM_BUCKET_FILL_FISH, SoundEvents.ITEM_BUCKET_FILL_LAVA, "item.bucket.fill_lava");
        insertAfter(registry, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, "item.chorus_fruit.teleport");
        insertAfter(registry, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundEvents.ITEM_ELYTRA_FLYING, "item.elytra.flying");
        insertAfter(registry, SoundEvents.ITEM_ELYTRA_FLYING, SoundEvents.ITEM_FIRECHARGE_USE, "item.firecharge.use");
        insertAfter(registry, SoundEvents.ITEM_FIRECHARGE_USE, SoundEvents.ITEM_FLINTANDSTEEL_USE, "item.flintandsteel.use");
        insertAfter(registry, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundEvents.ITEM_HOE_TILL, "item.hoe.till");
        insertAfter(registry, SoundEvents.ITEM_HOE_TILL, SoundEvents.ITEM_SHIELD_BLOCK, "item.shield.block");
        insertAfter(registry, SoundEvents.ITEM_SHIELD_BLOCK, SoundEvents.ITEM_SHIELD_BREAK, "item.shield.break");
        insertAfter(registry, SoundEvents.ITEM_SHIELD_BREAK, SoundEvents.ITEM_SHOVEL_FLATTEN, "item.shovel.flatten");
        insertAfter(registry, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundEvents.ITEM_TOTEM_USE, "item.totem.use");
        insertAfter(registry, SoundEvents.ITEM_TOTEM_USE, SoundEvents.ITEM_TRIDENT_HIT, "item.trident.hit");
        insertAfter(registry, SoundEvents.ITEM_TRIDENT_HIT, SoundEvents.ITEM_TRIDENT_HIT_GROUND, "item.trident.hit_ground");
        insertAfter(registry, SoundEvents.ITEM_TRIDENT_HIT_GROUND, SoundEvents.ITEM_TRIDENT_RETURN, "item.trident.return");
        insertAfter(registry, SoundEvents.ITEM_TRIDENT_RETURN, SoundEvents.ITEM_TRIDENT_RIPTIDE_1, "item.trident.riptide_1");
        insertAfter(registry, SoundEvents.ITEM_TRIDENT_RIPTIDE_1, SoundEvents.ITEM_TRIDENT_RIPTIDE_2, "item.trident.riptide_2");
        insertAfter(registry, SoundEvents.ITEM_TRIDENT_RIPTIDE_2, SoundEvents.ITEM_TRIDENT_RIPTIDE_3, "item.trident.riptide_3");
        insertAfter(registry, SoundEvents.ITEM_TRIDENT_RIPTIDE_3, SoundEvents.ITEM_TRIDENT_THROW, "item.trident.throw");
        insertAfter(registry, SoundEvents.ITEM_TRIDENT_THROW, SoundEvents.ITEM_TRIDENT_THUNDER, "item.trident.thunder");
        insertAfter(registry, SoundEvents.ITEM_TRIDENT_THUNDER, SoundEvents.MUSIC_CREATIVE, "music.creative");
        insertAfter(registry, SoundEvents.MUSIC_CREATIVE, SoundEvents.MUSIC_CREDITS, "music.credits");
        insertAfter(registry, SoundEvents.MUSIC_CREDITS, SoundEvents.MUSIC_DRAGON, "music.dragon");
        insertAfter(registry, SoundEvents.MUSIC_DRAGON, SoundEvents.MUSIC_END, "music.end");
        insertAfter(registry, SoundEvents.MUSIC_END, SoundEvents.MUSIC_GAME, "music.game");
        insertAfter(registry, SoundEvents.MUSIC_GAME, SoundEvents.MUSIC_MENU, "music.menu");
        insertAfter(registry, SoundEvents.MUSIC_MENU, SoundEvents.MUSIC_NETHER, "music.nether");
        insertAfter(registry, SoundEvents.MUSIC_NETHER, SoundEvents.MUSIC_UNDER_WATER, "music.under_water");
        insertAfter(registry, SoundEvents.MUSIC_UNDER_WATER, SoundEvents.MUSIC_DISC_11, "music_disc.11");
        insertAfter(registry, SoundEvents.MUSIC_DISC_11, SoundEvents.MUSIC_DISC_13, "music_disc.13");
        insertAfter(registry, SoundEvents.MUSIC_DISC_13, SoundEvents.MUSIC_DISC_BLOCKS, "music_disc.blocks");
        insertAfter(registry, SoundEvents.MUSIC_DISC_BLOCKS, SoundEvents.MUSIC_DISC_CAT, "music_disc.cat");
        insertAfter(registry, SoundEvents.MUSIC_DISC_CAT, SoundEvents.MUSIC_DISC_CHIRP, "music_disc.chirp");
        insertAfter(registry, SoundEvents.MUSIC_DISC_CHIRP, SoundEvents.MUSIC_DISC_FAR, "music_disc.far");
        insertAfter(registry, SoundEvents.MUSIC_DISC_FAR, SoundEvents.MUSIC_DISC_MALL, "music_disc.mall");
        insertAfter(registry, SoundEvents.MUSIC_DISC_MALL, SoundEvents.MUSIC_DISC_MELLOHI, "music_disc.mellohi");
        insertAfter(registry, SoundEvents.MUSIC_DISC_MELLOHI, SoundEvents.MUSIC_DISC_STAL, "music_disc.stal");
        insertAfter(registry, SoundEvents.MUSIC_DISC_STAL, SoundEvents.MUSIC_DISC_STRAD, "music_disc.strad");
        insertAfter(registry, SoundEvents.MUSIC_DISC_STRAD, SoundEvents.MUSIC_DISC_WAIT, "music_disc.wait");
        insertAfter(registry, SoundEvents.MUSIC_DISC_WAIT, SoundEvents.MUSIC_DISC_WARD, "music_disc.ward");
        insertAfter(registry, SoundEvents.MUSIC_DISC_WARD, SoundEvents.UI_BUTTON_CLICK, "ui.button.click");
        insertAfter(registry, SoundEvents.UI_BUTTON_CLICK, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, "ui.toast.challenge_complete");
        insertAfter(registry, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundEvents.UI_TOAST_IN, "ui.toast.in");
        insertAfter(registry, SoundEvents.UI_TOAST_IN, SoundEvents.UI_TOAST_OUT, "ui.toast.out");
        insertAfter(registry, SoundEvents.UI_TOAST_OUT, SoundEvents.WEATHER_RAIN, "weather.rain");
        insertAfter(registry, SoundEvents.WEATHER_RAIN, SoundEvents.WEATHER_RAIN_ABOVE, "weather.rain.above");
    }

    @Override
    public boolean acceptBlockState(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.NOTE_BLOCK) {
            Instrument instrument = state.get(NoteBlock.INSTRUMENT);
            if (instrument == Instrument.IRON_XYLOPHONE || instrument == Instrument.COW_BELL
                    || instrument == Instrument.DIDGERIDOO || instrument == Instrument.BIT
                    || instrument == Instrument.BANJO || instrument == Instrument.PLING)
                return false;
        }

        return super.acceptBlockState(state);
    }
}
