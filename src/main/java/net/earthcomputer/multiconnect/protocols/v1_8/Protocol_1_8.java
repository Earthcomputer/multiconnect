package net.earthcomputer.multiconnect.protocols.v1_8;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.v1_12.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.*;
import net.earthcomputer.multiconnect.protocols.v1_9.Protocol_1_9;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.Set;
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
