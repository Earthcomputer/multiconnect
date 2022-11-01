package net.earthcomputer.multiconnect.protocols.v1_8;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.v1_12.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.*;
import net.earthcomputer.multiconnect.protocols.v1_9.Protocol_1_9;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.Set;

public class Protocol_1_8 extends Protocol_1_9 {
    public static final int LEVEL_EVENT_QUIET_GHAST_SHOOT = -1000 + 1;
    private static final EntityDimensions DEFAULT_BOAT_DIMENSIONS = EntityType.BOAT.getDimensions();

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
}
