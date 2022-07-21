package net.earthcomputer.multiconnect.protocols.v1_8;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.earthcomputer.multiconnect.protocols.v1_12.block.Blocks_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12.Protocol_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_13.Protocol_1_13_2;
import net.earthcomputer.multiconnect.protocols.v1_14.Protocol_1_14_4;
import net.earthcomputer.multiconnect.protocols.v1_15.mixin.TamableAnimalAccessor;
import net.earthcomputer.multiconnect.protocols.v1_18.Protocol_1_18_2;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.*;
import net.earthcomputer.multiconnect.protocols.v1_9.Protocol_1_9;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Protocol_1_8 extends Protocol_1_9 {

    private static final AtomicInteger FAKE_TELEPORT_ID_COUNTER = new AtomicInteger();
    public static final int LEVEL_EVENT_QUIET_GHAST_SHOOT = -1000 + 1;
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
        stack.addTagElement("multiconnect:1.8/potionData", ShortTag.valueOf((short) meta));
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
            newStack.setTag(stack.getTag());
            stack = newStack;
        }
        PotionUtils.setPotion(stack, potion);
        return stack;
    }

    public static Pair<ItemStack, Integer> newPotionItemToOld(ItemStack stack) {
        Potion potion = PotionUtils.getPotion(stack);
        CompoundTag tag = stack.getTag();
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
                stack.setTag(null);
            }
        }

        boolean isSplash = stack.getItem() == Items.SPLASH_POTION;
        if (isSplash) {
            ItemStack newStack = new ItemStack(Items.POTION, stack.getCount());
            newStack.setTag(stack.getTag());
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
    public void setup() {
        super.setup();
        ((EntityTypeAccessor) EntityType.BOAT).setDimensions(EntityDimensions.scalable(1.5f, 0.5625f));
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
    public void registerCommands(CommandDispatcher<SharedSuggestionProvider> dispatcher, @Nullable Set<String> serverCommands) {
        super.registerCommands(dispatcher, serverCommands);
        BrigadierRemover.of(dispatcher).get("time").get("query").get("day").remove();
        BrigadierRemover.of(dispatcher).get("scoreboard").get("players").get("tag").remove();
        BrigadierRemover.of(dispatcher).get("scoreboard").get("teams").get("option").get("team").get("collisionRule").remove();
    }

    public static List<SynchedEntityData.DataItem<?>> deserializeDataTrackerEntries(FriendlyByteBuf buf) {
        ArrayList<SynchedEntityData.DataItem<?>> entries = null;

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
                case 4 -> buf.readUtf(32767);
                case 5 -> buf.readItem();
                case 6 -> new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
                case 7 -> new Rotations(buf.readFloat(), buf.readFloat(), buf.readFloat());
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
            if (entity instanceof RemotePlayer player) {
                if (usingItem) {
                    player.startUsingItem(InteractionHand.MAIN_HAND);
                } else {
                    player.stopUsingItem();
                }
            }
            entity.getEntityData().set(EntityAccessor.getDataSharedFlagsId(), data);
        } else if (id == 3) {
            entity.setCustomNameVisible(data == 1);
        } else if (id == 4) {
            entity.setSilent(data == 1);
        } else if (entity instanceof LivingEntity) {
            if (id == 8) {
                entity.getEntityData().set(LivingEntityAccessor.getDataEffectAmbienceId(), data > 0);
            } else if (id == 9) {
                ((LivingEntity) entity).setArrowCount(data);
            } else if (entity instanceof Mob) {
                if (id == 15) {
                    entity.getEntityData().set(MobAccessor.getDataMobFlagsId(), data);
                } else if (entity instanceof AgeableMob) {
                    if (id == 12) {
                        entity.getEntityData().set(AgeableMobAccessor.getDataBabyId(), data < 0);
                    } else if (entity instanceof AbstractHorse) {
                        if (id == 19) {
                            entity.getEntityData().set(Protocol_1_10.OLD_HORSE_TYPE, (int) data);
                        }
                    } else if (entity instanceof Pig) {
                        if (id == 16) {
                            entity.getEntityData().set(PigAccessor.getDataSaddleId(), data != 0);
                        }
                    } else if (entity instanceof Rabbit) {
                        if (id == 18) {
                            entity.getEntityData().set(RabbitAccessor.getDataTypeId(), (int) data);
                        }
                    } else if (entity instanceof Sheep) {
                        if (id == 16) {
                            entity.getEntityData().set(SheepAccessor.getDataWoolId(), data);
                        }
                    } else if (entity instanceof TamableAnimal) {
                        if (id == 16) {
                            entity.getEntityData().set(TamableAnimalAccessor.getDataFlagsId(), data);
                        } else if (entity instanceof Wolf) {
                            if (id == 19) {
                                ((Wolf) entity).setIsInterested(data == 1);
                            } else if (id == 20) {
                                entity.getEntityData().set(Protocol_1_12_2.OLD_WOLF_COLLAR_COLOR, (int) data);
                            }
                        }
                    }
                } else if (entity instanceof Bat) {
                    if (id == 16) {
                        entity.getEntityData().set(BatAccessor.getDataIdFlags(), data);
                    }
                } else if (entity instanceof Blaze) {
                    if (id == 16) {
                        entity.getEntityData().set(BlazeAccessor.getDataFlagsId(), data);
                    }
                } else if (entity instanceof Creeper) {
                    if (id == 17) {
                        entity.getEntityData().set(CreeperAccessor.getDataIsPowered(), data == 1);
                    } else if (id == 18) {
                        entity.getEntityData().set(CreeperAccessor.getDataIsIgnited(), data == 1);
                    }
                } else if (entity instanceof EnderMan) {
                    if (id == 18) {
                        entity.getEntityData().set(EnderManAccessor.getDataCreepy(), data > 0);
                    }
                } else if (entity instanceof Ghast) {
                    if (id == 16) {
                        ((Ghast) entity).setCharging(data != 0);
                    }
                } else if (entity instanceof IronGolem) {
                    if (id == 16) {
                        entity.getEntityData().set(IronGolemAccessor.getDataFlagsId(), data);
                    }
                } else if (entity instanceof AbstractSkeleton) {
                    if (id == 13) {
                        entity.getEntityData().set(Protocol_1_10.OLD_SKELETON_TYPE, (int) data);
                    }
                } else if (entity instanceof Slime) {
                    if (id == 16) {
                        entity.getEntityData().set(SlimeAccessor.getIdSize(), (int) data);
                    }
                } else if (entity instanceof Spider) {
                    if (id == 16) {
                        entity.getEntityData().set(SpiderAccessor.getDataFlagsId(), data);
                    }
                } else if (entity instanceof Witch) {
                    if (id == 21) {
                        ((Witch) entity).setUsingItem(data == 1);
                    }
                } else if (entity instanceof Zombie) {
                    if (id == 12) {
                        entity.getEntityData().set(ZombieAccessor.getDataBabyId(), data == 1);
                    } else if (id == 13) {
                        entity.getEntityData().set(Protocol_1_10.OLD_ZOMBIE_TYPE, (int) data);
                    } else if (id == 14) {
                        entity.getEntityData().set(Protocol_1_10.OLD_ZOMBIE_CONVERTING, data == 1);
                    }
                }
            } else if (entity instanceof ArmorStand) {
                if (id == 10) {
                    entity.getEntityData().set(ArmorStand.DATA_CLIENT_FLAGS, data);
                }
            } else if (entity instanceof Player) {
                if (id == 10) {
                    entity.getEntityData().set(PlayerAccessor.getDataPlayerModeCustomisation(), data);
                }
            }
        } else if (entity instanceof Projectile) {
            if (id == 16) {
                entity.getEntityData().set(AbstractArrowAccessor.getIdFlags(), data);
            } else if (entity instanceof WitherSkull) {
                if (id == 10) {
                    ((WitherSkull) entity).setDangerous(data == 1);
                }
            }
        } else if (entity instanceof ItemFrame) {
            if (id == 9) {
                entity.getEntityData().set(ItemFrameAccessor.getDataRotation(), (int)data);
            }
        } else if (entity instanceof AbstractMinecart) {
            if (id == 22) {
                ((AbstractMinecart) entity).setCustomDisplay(data == 1);
            } else if (entity instanceof MinecartFurnace) {
                if (id == 16) {
                    entity.getEntityData().set(MinecartFurnaceAccessor.getDataIdFuel(), data != 0);
                }
            }
        }
    }

    public static void handleShortTrackedData(Entity entity, int id, short data) {
        if (id == 1) {
            entity.setAirSupply(data);
        } else if (entity instanceof EnderMan enderman) {
            if (id == 16) {
                BlockState heldState = Block.BLOCK_STATE_REGISTRY.byId(Blocks_1_12_2.convertToStateRegistryId(data));
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
                entity.getEntityData().set(LivingEntityAccessor.getDataEffectColorId(), data);
            } else if (entity instanceof Creeper) {
                if (id == 16) {
                    ((Creeper) entity).setSwellDir(data);
                }
            } else if (entity instanceof Guardian) {
                if (id == 16) {
                    entity.getEntityData().set(Protocol_1_10.OLD_GUARDIAN_FLAGS, (byte) data);
                } else if (id == 17) {
                    entity.getEntityData().set(GuardianAccessor.getDataIdAttackTarget(), data);
                }
            } else if (entity instanceof AbstractHorse) {
                if (id == 16) {
                    entity.getEntityData().set(Protocol_1_10.OLD_HORSE_FLAGS, (byte) data);
                } else if (id == 20) {
                    entity.getEntityData().set(Protocol_1_10.OLD_HORSE_VARIANT, data);
                } else if (id == 22) {
                    entity.getEntityData().set(Protocol_1_10.OLD_HORSE_ARMOR, data);
                }
            } else if (entity instanceof Cat) {
                if (id == 18) {
                    entity.getEntityData().set(Protocol_1_18_2.OLD_CAT_VARIANT, data);
                }
            } else if (entity instanceof Player) {
                if (id == 18) {
                    ((Player) entity).setScore(data);
                }
            } else if (entity instanceof Villager) {
                if (id == 16) {
                    entity.getEntityData().set(Protocol_1_13_2.OLD_VILLAGER_PROFESSION, data);
                }
            } else if (entity instanceof WitherBoss wither) {
                if (id >= 17 && id <= 19) {
                    wither.setAlternativeTarget(id - 17, data);
                } else if (id == 20) {
                    wither.setInvulnerableTicks(data);
                }
            }
        } else if (entity instanceof Boat boat) {
            if (id == 17) {
                boat.setHurtTime(data);
            } else if (id == 18) {
                boat.setHurtDir(data);
            }
        } else if (entity instanceof EndCrystal) {
            if (id == 8) {
                // TODO: health??
            }
        } else if (entity instanceof AbstractMinecart minecart) {
            if (id == 17) {
                minecart.setHurtTime(data);
            } else if (id == 18) {
                minecart.setHurtDir(data);
            } else if (id == 20) {
                entity.getEntityData().set(Protocol_1_12_2.OLD_MINECART_DISPLAY_TILE, data);
            } else if (id == 21) {
                minecart.setDisplayOffset(data);
            }
        }
    }

    public static void handleFloatTrackedData(Entity entity, int id, float data) {
        if (entity instanceof LivingEntity) {
            if (id == 6) {
                entity.getEntityData().set(LivingEntityAccessor.getDataHealthId(), data);
            } else if (entity instanceof Player) {
                if (id == 17) {
                    entity.getEntityData().set(PlayerAccessor.getDataPlayerAbsorptionId(), data);
                }
            } else if (entity instanceof Wolf) {
                if (id == 18) {
                    entity.getEntityData().set(Protocol_1_14_4.OLD_WOLF_HEALTH, data);
                }
            }
        } else if (entity instanceof Boat) {
            if (id == 19) {
                ((Boat) entity).setDamage(data);
            }
        } else if (entity instanceof Minecart) {
            if (id == 19) {
                ((Minecart) entity).setDamage(data);
            }
        }
    }

    public static void handleStringTrackedData(Entity entity, int id, String data) {
        if (id == 2) {
            entity.getEntityData().set(Protocol_1_12_2.OLD_CUSTOM_NAME, data);
        } else if (entity instanceof AbstractHorse horse) {
            if (id == 21) {
                if (data.isEmpty()) {
                    horse.setOwnerUUID(null);
                } else {
                    try {
                        horse.setOwnerUUID(UUID.fromString(data));
                    } catch (IllegalArgumentException e) {
                        horse.setOwnerUUID(null);
                    }
                }
            }
        } else if (entity instanceof MinecartCommandBlock) {
            if (id == 23) {
                entity.getEntityData().set(MinecartCommandBlockAccessor.getDataIdCommandName(), data);
            } else if (id == 24) {
                entity.getEntityData().set(MinecartCommandBlockAccessor.getDataIdLastOutput(), Component.literal(data));
            }
        } else if (entity instanceof TamableAnimal tameable) {
            if (id == 17) {
                if (data.isEmpty()) {
                    tameable.setOwnerUUID(null);
                } else {
                    try {
                        tameable.setOwnerUUID(UUID.fromString(data));
                    } catch (IllegalArgumentException e) {
                        tameable.setOwnerUUID(null);
                    }
                }
            }
        }
    }

    public static void handleItemStackTrackedData(Entity entity, int id, ItemStack data) {
        if (entity instanceof FireworkRocketEntity) {
            if (id == 8) {
                entity.getEntityData().set(FireworkRocketEntityAccessor.getDataIdFireworksItem(), data);
            }
        } else if (entity instanceof ItemFrame) {
            if (id == 8) {
                entity.getEntityData().set(ItemFrameAccessor.getDataItem(), data);
            }
        } else if (entity instanceof ItemEntity) {
            if (id == 10) {
                ((ItemEntity) entity).setItem(data);
            }
        }
    }

    public static void handleBlockPosTrackedData(Entity entity, int id, BlockPos data) {

    }

    public static void handleEulerAngleTrackedData(Entity entity, int id, Rotations data) {
        if (entity instanceof ArmorStand) {
            switch (id) {
                case 11 -> entity.getEntityData().set(ArmorStand.DATA_HEAD_POSE, data);
                case 12 -> entity.getEntityData().set(ArmorStand.DATA_BODY_POSE, data);
                case 13 -> entity.getEntityData().set(ArmorStand.DATA_LEFT_ARM_POSE, data);
                case 14 -> entity.getEntityData().set(ArmorStand.DATA_RIGHT_ARM_POSE, data);
                case 15 -> entity.getEntityData().set(ArmorStand.DATA_LEFT_LEG_POSE, data);
                case 16 -> entity.getEntityData().set(ArmorStand.DATA_RIGHT_LEG_POSE, data);
            }
        }
    }

    public static OptionalDouble getDefaultAttackDamage(Item item) {
        if (item instanceof TieredItem tieredItem) {
            Tier tier = tieredItem.getTier();
            int materialBonus;
            if (tier == Tiers.STONE) {
                materialBonus = 1;
            } else if (tier == Tiers.IRON) {
                materialBonus = 2;
            } else if (tier == Tiers.DIAMOND) {
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
    public int clientSlotIdToServer(AbstractContainerMenu menu, int slotId) {
        slotId = super.clientSlotIdToServer(menu, slotId);
        if (slotId == -1) {
            return -1;
        }
        if (menu instanceof BrewingStandMenu) {
            if (slotId == 4) { // fuel slot
                return -1;
            } else if (slotId > 4) {
                slotId--;
            }
        }
        return slotId;
    }

    @Override
    public int serverSlotIdToClient(AbstractContainerMenu menu, int slotId) {
        if (menu instanceof BrewingStandMenu && slotId >= 4) {
            slotId++;
        }
        return super.serverSlotIdToClient(menu, slotId);
    }
}
