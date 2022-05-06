package net.earthcomputer.multiconnect.protocols.v1_8;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.earthcomputer.multiconnect.protocols.v1_12_2.Blocks_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12_2.Protocol_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.earthcomputer.multiconnect.protocols.v1_14_4.Protocol_1_14_4;
import net.earthcomputer.multiconnect.protocols.v1_15_2.mixin.TameableEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.*;
import net.earthcomputer.multiconnect.protocols.v1_9.Protocol_1_9;
import net.minecraft.block.*;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtShort;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EulerAngle;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Protocol_1_8 extends Protocol_1_9 {

    private static final AtomicInteger FAKE_TELEPORT_ID_COUNTER = new AtomicInteger();
    public static final int WORLD_EVENT_QUIET_GHAST_SHOOT = -1000 + 1;
    private static final EntityDimensions DEFAULT_BOAT_DIMENSIONS = EntityType.BOAT.getDimensions();

    private static final BiMap<Potion, Integer> POTION_METAS = ImmutableBiMap.<Potion, Integer>builder()
            .put(Potions.SWIFTNESS, 2)
            .put(Potions.STRONG_SWIFTNESS, 2 | 32)
            .put(Potions.LONG_SWIFTNESS, 2 | 64)
            .put(Potions.SLOWNESS, 10)
            .put(Potions.STRONG_SLOWNESS, 10 | 32)
            .put(Potions.LONG_SLOWNESS, 10 | 64)
            .put(Potions.STRENGTH, 9)
            .put(Potions.STRONG_STRENGTH, 9 | 32)
            .put(Potions.LONG_STRENGTH, 9 | 64)
            .put(Potions.HEALING, 5)
            .put(Potions.STRONG_HEALING, 5 | 32)
            .put(Potions.HARMING, 12)
            .put(Potions.STRONG_HARMING, 12 | 32)
            .put(Potions.LEAPING, 11)
            .put(Potions.STRONG_LEAPING, 11 | 32)
            .put(Potions.LONG_LEAPING, 11 | 64)
            .put(Potions.REGENERATION, 1)
            .put(Potions.STRONG_REGENERATION, 1 | 32)
            .put(Potions.LONG_REGENERATION, 1 | 64)
            .put(Potions.FIRE_RESISTANCE, 3)
            .put(Potions.LONG_FIRE_RESISTANCE, 3 | 64)
            .put(Potions.WATER_BREATHING, 13)
            .put(Potions.LONG_WATER_BREATHING, 13 | 64)
            .put(Potions.INVISIBILITY, 14)
            .put(Potions.LONG_INVISIBILITY, 14 | 64)
            .put(Potions.NIGHT_VISION, 6)
            .put(Potions.LONG_NIGHT_VISION, 6 | 64)
            .put(Potions.WEAKNESS, 8)
            .put(Potions.LONG_WEAKNESS, 8 | 64)
            .put(Potions.POISON, 4)
            .put(Potions.STRONG_POISON, 4 | 32)
            .put(Potions.LONG_POISON, 4 | 64)
            .build();

    public static ItemStack oldPotionItemToNew(ItemStack stack, int meta) {
        stack.setSubNbt("multiconnect:1.8/potionData", NbtShort.of((short) meta));
        boolean isSplash = (meta & 16384) != 0;
        Potion potion;
        if (meta == 0) {
            potion = Potions.WATER;
        } else if (meta == 16) {
            potion = Potions.AWKWARD;
        } else if (meta == 32) {
            potion = Potions.THICK;
        } else if (meta == 64) {
            potion = Potions.MUNDANE;
        } else if (meta == 8192) {
            potion = Potions.MUNDANE;
        } else {
            potion = POTION_METAS.inverse().getOrDefault(meta & 127, Potions.EMPTY);
        }
        if (isSplash) {
            ItemStack newStack = new ItemStack(Items.SPLASH_POTION, stack.getCount());
            newStack.setNbt(stack.getNbt());
            stack = newStack;
        }
        PotionUtil.setPotion(stack, potion);
        return stack;
    }

    public static Pair<ItemStack, Integer> newPotionItemToOld(ItemStack stack) {
        Potion potion = PotionUtil.getPotion(stack);
        NbtCompound tag = stack.getNbt();
        boolean hasForcedMeta = false;
        int forcedMeta = 0;
        if (tag != null) {
            tag.remove("Potion");
            if (tag.contains("multiconnect:1.8/potionData", 2)) { // short
                hasForcedMeta = true;
                forcedMeta = tag.getShort("multiconnect:1.8/potionData") & 0xffff;
                tag.remove("multiconnect:1.8/potionData");
            }
            if (tag.isEmpty()) {
                stack.setNbt(null);
            }
        }

        boolean isSplash = stack.getItem() == Items.SPLASH_POTION;
        if (isSplash) {
            ItemStack newStack = new ItemStack(Items.POTION, stack.getCount());
            newStack.setNbt(stack.getNbt());
            stack = newStack;
        }

        if (hasForcedMeta) {
            return Pair.of(stack, forcedMeta);
        }

        int meta;
        if (potion == Potions.WATER) {
            meta = 0;
        } else if (potion == Potions.AWKWARD) {
            meta = 16;
        } else if (potion == Potions.THICK) {
            meta = 32;
        } else if (potion == Potions.MUNDANE) {
            meta = 8192;
        } else {
            meta = POTION_METAS.getOrDefault(potion, 0);
            if (isSplash) {
                meta |= 16384;
            } else {
                meta |= 8192;
            }
        }

        return Pair.of(stack, meta);
    }

    @Override
    public void setup(boolean resourceReload) {
        super.setup(resourceReload);
        ((EntityTypeAccessor) EntityType.BOAT).setDimensions(EntityDimensions.changing(1.5f, 0.5625f));
    }

    @Override
    public void disable() {
        ((EntityTypeAccessor) EntityType.BOAT).setDimensions(DEFAULT_BOAT_DIMENSIONS);
        super.disable();
    }

    @Override
    protected void markChangedCollisionBoxes() {
        super.markChangedCollisionBoxes();
        markCollisionBoxChanged(Blocks.LADDER);
        markCollisionBoxChanged(Blocks.LILY_PAD);
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSource> dispatcher, Set<String> serverCommands) {
        super.registerCommands(dispatcher, serverCommands);
        BrigadierRemover.of(dispatcher).get("time").get("query").get("day").remove();
        BrigadierRemover.of(dispatcher).get("scoreboard").get("players").get("tag").remove();
        BrigadierRemover.of(dispatcher).get("scoreboard").get("teams").get("option").get("team").get("collisionRule").remove();
    }

    public static List<DataTracker.Entry<?>> deserializeDataTrackerEntries(PacketByteBuf buf) {
        ArrayList<DataTracker.Entry<?>> entries = null;

        int n;
        while ((n = buf.readByte()) != 127) {
            if (entries == null) {
                entries = new ArrayList<>();
            }

            int serializerId = (n & 0b11100000) >> 5;
            int id = n & 0b00011111;
            Object value = switch (serializerId) {
                case 0 -> buf.readByte();
                case 1 -> buf.readShort();
                case 2 -> buf.readInt();
                case 3 -> buf.readFloat();
                case 4 -> buf.readString(32767);
                case 5 -> buf.readItemStack();
                case 6 -> new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
                case 7 -> new EulerAngle(buf.readFloat(), buf.readFloat(), buf.readFloat());
                default ->
                        // serializer id ranges from 0-7
                        throw new AssertionError();
            };

            entries.add(new DataTrackerEntry_1_8(serializerId, id, value));
        }

        return entries;
    }

    public static void handleByteTrackedData(Entity entity, int id, byte data) {
        if (id == 0) {
            boolean usingItem = (data & 16) != 0;
            if (usingItem) {
                data &= ~16;
            }
            if (entity instanceof OtherClientPlayerEntity player) {
                if (usingItem) {
                    player.setCurrentHand(Hand.MAIN_HAND);
                } else {
                    player.clearActiveItem();
                }
            }
            entity.getDataTracker().set(EntityAccessor.getFlags(), data);
        } else if (id == 3) {
            entity.setCustomNameVisible(data == 1);
        } else if (id == 4) {
            entity.setSilent(data == 1);
        } else if (entity instanceof LivingEntity) {
            if (id == 8) {
                entity.getDataTracker().set(LivingEntityAccessor.getPotionSwirlsAmbient(), data > 0);
            } else if (id == 9) {
                ((LivingEntity) entity).setStuckArrowCount(data);
            } else if (entity instanceof MobEntity) {
                if (id == 15) {
                    entity.getDataTracker().set(MobEntityAccessor.getMobFlags(), data);
                } else if (entity instanceof PassiveEntity) {
                    if (id == 12) {
                        entity.getDataTracker().set(PassiveEntityAccessor.getChild(), data < 0);
                    } else if (entity instanceof HorseBaseEntity) {
                        if (id == 19) {
                            entity.getDataTracker().set(Protocol_1_10.OLD_HORSE_TYPE, (int) data);
                        }
                    } else if (entity instanceof PigEntity) {
                        if (id == 16) {
                            entity.getDataTracker().set(PigEntityAccessor.getSaddled(), data != 0);
                        }
                    } else if (entity instanceof RabbitEntity) {
                        if (id == 18) {
                            entity.getDataTracker().set(RabbitEntityAccessor.getRabbitType(), (int) data);
                        }
                    } else if (entity instanceof SheepEntity) {
                        if (id == 16) {
                            entity.getDataTracker().set(SheepEntityAccessor.getColor(), data);
                        }
                    } else if (entity instanceof TameableEntity) {
                        if (id == 16) {
                            entity.getDataTracker().set(TameableEntityAccessor.getTameableFlags(), data);
                        } else if (entity instanceof WolfEntity) {
                            if (id == 19) {
                                ((WolfEntity) entity).setBegging(data == 1);
                            } else if (id == 20) {
                                entity.getDataTracker().set(Protocol_1_12_2.OLD_WOLF_COLLAR_COLOR, (int) data);
                            }
                        }
                    }
                } else if (entity instanceof BatEntity) {
                    if (id == 16) {
                        entity.getDataTracker().set(BatEntityAccessor.getBatFlags(), data);
                    }
                } else if (entity instanceof BlazeEntity) {
                    if (id == 16) {
                        entity.getDataTracker().set(BlazeEntityAccessor.getBlazeFlags(), data);
                    }
                } else if (entity instanceof CreeperEntity) {
                    if (id == 17) {
                        entity.getDataTracker().set(CreeperEntityAccessor.getCharged(), data == 1);
                    } else if (id == 18) {
                        entity.getDataTracker().set(CreeperEntityAccessor.getIgnited(), data == 1);
                    }
                } else if (entity instanceof EndermanEntity) {
                    if (id == 18) {
                        entity.getDataTracker().set(EndermanEntityAccessor.getAngry(), data > 0);
                    }
                } else if (entity instanceof GhastEntity) {
                    if (id == 16) {
                        ((GhastEntity) entity).setShooting(data != 0);
                    }
                } else if (entity instanceof IronGolemEntity) {
                    if (id == 16) {
                        entity.getDataTracker().set(IronGolemEntityAccessor.getIronGolemFlags(), data);
                    }
                } else if (entity instanceof AbstractSkeletonEntity) {
                    if (id == 13) {
                        entity.getDataTracker().set(Protocol_1_10.OLD_SKELETON_TYPE, (int) data);
                    }
                } else if (entity instanceof SlimeEntity) {
                    if (id == 16) {
                        entity.getDataTracker().set(SlimeEntityAccessor.getSlimeSize(), (int) data);
                    }
                } else if (entity instanceof SpiderEntity) {
                    if (id == 16) {
                        entity.getDataTracker().set(SpiderEntityAccessor.getSpiderFlags(), data);
                    }
                } else if (entity instanceof WitchEntity) {
                    if (id == 21) {
                        ((WitchEntity) entity).setDrinking(data == 1);
                    }
                } else if (entity instanceof ZombieEntity) {
                    if (id == 12) {
                        entity.getDataTracker().set(ZombieEntityAccessor.getBaby(), data == 1);
                    } else if (id == 13) {
                        entity.getDataTracker().set(Protocol_1_10.OLD_ZOMBIE_TYPE, (int) data);
                    } else if (id == 14) {
                        entity.getDataTracker().set(Protocol_1_10.OLD_ZOMBIE_CONVERTING, data == 1);
                    }
                }
            } else if (entity instanceof ArmorStandEntity) {
                if (id == 10) {
                    entity.getDataTracker().set(ArmorStandEntity.ARMOR_STAND_FLAGS, data);
                }
            } else if (entity instanceof PlayerEntity) {
                if (id == 10) {
                    entity.getDataTracker().set(PlayerEntityAccessor.getPlayerModelParts(), data);
                }
            }
        } else if (entity instanceof ProjectileEntity) {
            if (id == 16) {
                entity.getDataTracker().set(PersistentProjectileEntityAccessor.getProjectileFlags(), data);
            } else if (entity instanceof WitherSkullEntity) {
                if (id == 10) {
                    ((WitherSkullEntity) entity).setCharged(data == 1);
                }
            }
        } else if (entity instanceof ItemFrameEntity) {
            if (id == 9) {
                entity.getDataTracker().set(ItemFrameEntityAccessor.getRotation(), (int)data);
            }
        } else if (entity instanceof AbstractMinecartEntity) {
            if (id == 22) {
                ((AbstractMinecartEntity) entity).setCustomBlockPresent(data == 1);
            } else if (entity instanceof FurnaceMinecartEntity) {
                if (id == 16) {
                    entity.getDataTracker().set(FurnaceMinecartEntityAccessor.getLit(), data != 0);
                }
            }
        }
    }

    public static void handleShortTrackedData(Entity entity, int id, short data) {
        if (id == 1) {
            entity.setAir(data);
        } else if (entity instanceof EndermanEntity enderman) {
            if (id == 16) {
                BlockState heldState = Block.STATE_IDS.get(Blocks_1_12_2.convertToStateRegistryId(data));
                if (heldState == null || heldState.isAir()) {
                    enderman.setCarriedBlock(null);
                } else {
                    enderman.setCarriedBlock(heldState);
                }
            }
        }
    }

    public static void handleIntTrackedData(Entity entity, int id, int data) {
        if (entity instanceof LivingEntity) {
            if (id == 7) {
                entity.getDataTracker().set(LivingEntityAccessor.getPotionSwirlsColor(), data);
            } else if (entity instanceof CreeperEntity) {
                if (id == 16) {
                    ((CreeperEntity) entity).setFuseSpeed(data);
                }
            } else if (entity instanceof GuardianEntity) {
                if (id == 16) {
                    entity.getDataTracker().set(Protocol_1_10.OLD_GUARDIAN_FLAGS, (byte) data);
                } else if (id == 17) {
                    entity.getDataTracker().set(GuardianEntityAccessor.getBeamTargetId(), data);
                }
            } else if (entity instanceof HorseBaseEntity) {
                if (id == 16) {
                    entity.getDataTracker().set(Protocol_1_10.OLD_HORSE_FLAGS, (byte) data);
                } else if (id == 20) {
                    entity.getDataTracker().set(Protocol_1_10.OLD_HORSE_VARIANT, data);
                } else if (id == 22) {
                    entity.getDataTracker().set(Protocol_1_10.OLD_HORSE_ARMOR, data);
                }
            } else if (entity instanceof CatEntity) {
                if (id == 18) {
                    entity.getDataTracker().set(CatEntityAccessor.getCatType(), data);
                }
            } else if (entity instanceof PlayerEntity) {
                if (id == 18) {
                    ((PlayerEntity) entity).setScore(data);
                }
            } else if (entity instanceof VillagerEntity) {
                if (id == 16) {
                    entity.getDataTracker().set(Protocol_1_13_2.OLD_VILLAGER_PROFESSION, data);
                }
            } else if (entity instanceof WitherEntity wither) {
                if (id >= 17 && id <= 19) {
                    wither.setTrackedEntityId(id - 17, data);
                } else if (id == 20) {
                    wither.setInvulTimer(data);
                }
            }
        } else if (entity instanceof BoatEntity boat) {
            if (id == 17) {
                boat.setDamageWobbleTicks(data);
            } else if (id == 18) {
                boat.setDamageWobbleSide(data);
            }
        } else if (entity instanceof EndCrystalEntity) {
            if (id == 8) {
                // TODO: health??
            }
        } else if (entity instanceof AbstractMinecartEntity minecart) {
            if (id == 17) {
                minecart.setDamageWobbleTicks(data);
            } else if (id == 18) {
                minecart.setDamageWobbleSide(data);
            } else if (id == 20) {
                entity.getDataTracker().set(Protocol_1_12_2.OLD_MINECART_DISPLAY_TILE, data);
            } else if (id == 21) {
                minecart.setCustomBlockOffset(data);
            }
        }
    }

    public static void handleFloatTrackedData(Entity entity, int id, float data) {
        if (entity instanceof LivingEntity) {
            if (id == 6) {
                entity.getDataTracker().set(LivingEntityAccessor.getHealth(), data);
            } else if (entity instanceof PlayerEntity) {
                if (id == 17) {
                    entity.getDataTracker().set(PlayerEntityAccessor.getAbsorptionAmount(), data);
                }
            } else if (entity instanceof WolfEntity) {
                if (id == 18) {
                    entity.getDataTracker().set(Protocol_1_14_4.OLD_WOLF_HEALTH, data);
                }
            }
        } else if (entity instanceof BoatEntity) {
            if (id == 19) {
                ((BoatEntity) entity).setDamageWobbleStrength(data);
            }
        } else if (entity instanceof MinecartEntity) {
            if (id == 19) {
                ((MinecartEntity) entity).setDamageWobbleStrength(data);
            }
        }
    }

    public static void handleStringTrackedData(Entity entity, int id, String data) {
        if (id == 2) {
            entity.getDataTracker().set(Protocol_1_12_2.OLD_CUSTOM_NAME, data);
        } else if (entity instanceof HorseBaseEntity horse) {
            if (id == 21) {
                if (data.isEmpty()) {
                    horse.setOwnerUuid(null);
                } else {
                    try {
                        horse.setOwnerUuid(UUID.fromString(data));
                    } catch (IllegalArgumentException e) {
                        horse.setOwnerUuid(null);
                    }
                }
            }
        } else if (entity instanceof CommandBlockMinecartEntity) {
            if (id == 23) {
                entity.getDataTracker().set(CommandBlockMinecartEntityAccessor.getCommand(), data);
            } else if (id == 24) {
                entity.getDataTracker().set(CommandBlockMinecartEntityAccessor.getLastOutput(), new LiteralText(data));
            }
        } else if (entity instanceof TameableEntity tameable) {
            if (id == 17) {
                if (data.isEmpty()) {
                    tameable.setOwnerUuid(null);
                } else {
                    try {
                        tameable.setOwnerUuid(UUID.fromString(data));
                    } catch (IllegalArgumentException e) {
                        tameable.setOwnerUuid(null);
                    }
                }
            }
        }
    }

    public static void handleItemStackTrackedData(Entity entity, int id, ItemStack data) {
        if (entity instanceof FireworkRocketEntity) {
            if (id == 8) {
                entity.getDataTracker().set(FireworkRocketEntityAccessor.getItem(), data);
            }
        } else if (entity instanceof ItemFrameEntity) {
            if (id == 8) {
                entity.getDataTracker().set(ItemFrameEntityAccessor.getItemStack(), data);
            }
        } else if (entity instanceof ItemEntity) {
            if (id == 10) {
                ((ItemEntity) entity).setStack(data);
            }
        }
    }

    public static void handleBlockPosTrackedData(Entity entity, int id, BlockPos data) {

    }

    public static void handleEulerAngleTrackedData(Entity entity, int id, EulerAngle data) {
        if (entity instanceof ArmorStandEntity) {
            switch (id) {
                case 11 -> entity.getDataTracker().set(ArmorStandEntity.TRACKER_HEAD_ROTATION, data);
                case 12 -> entity.getDataTracker().set(ArmorStandEntity.TRACKER_BODY_ROTATION, data);
                case 13 -> entity.getDataTracker().set(ArmorStandEntity.TRACKER_LEFT_ARM_ROTATION, data);
                case 14 -> entity.getDataTracker().set(ArmorStandEntity.TRACKER_RIGHT_ARM_ROTATION, data);
                case 15 -> entity.getDataTracker().set(ArmorStandEntity.TRACKER_LEFT_LEG_ROTATION, data);
                case 16 -> entity.getDataTracker().set(ArmorStandEntity.TRACKER_RIGHT_LEG_ROTATION, data);
            }
        }
    }

    public static OptionalDouble getDefaultAttackDamage(Item item) {
        if (item instanceof ToolItem) {
            ToolMaterial material = ((ToolItem) item).getMaterial();
            int materialBonus;
            if (material == ToolMaterials.STONE) {
                materialBonus = 1;
            } else if (material == ToolMaterials.IRON) {
                materialBonus = 2;
            } else if (material == ToolMaterials.DIAMOND) {
                materialBonus = 3;
            } else {
                materialBonus = 0;
            }
            if (item instanceof SwordItem) {
                return OptionalDouble.of(4 + materialBonus);
            } else if (item instanceof PickaxeItem) {
                return OptionalDouble.of(2 + materialBonus);
            } else if (item instanceof ShovelItem) {
                return OptionalDouble.of(1 + materialBonus);
            } else if (item instanceof AxeItem) {
                return OptionalDouble.of(3 + materialBonus);
            }
        }

        return OptionalDouble.empty();
    }

    @Override
    public int clientSlotIdToServer(ScreenHandler screenHandler, int slotId) {
        slotId = super.clientSlotIdToServer(screenHandler, slotId);
        if (slotId == -1) {
            return -1;
        }
        if (screenHandler instanceof BrewingStandScreenHandler) {
            if (slotId == 4) { // fuel slot
                return -1;
            } else if (slotId > 4) {
                slotId--;
            }
        }
        return slotId;
    }

    @Override
    public int serverSlotIdToClient(ScreenHandler screenHandler, int slotId) {
        if (screenHandler instanceof BrewingStandScreenHandler && slotId >= 4) {
            slotId++;
        }
        return super.serverSlotIdToClient(screenHandler, slotId);
    }
}
