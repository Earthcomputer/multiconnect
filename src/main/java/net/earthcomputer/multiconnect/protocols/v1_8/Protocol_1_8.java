package net.earthcomputer.multiconnect.protocols.v1_8;

import net.earthcomputer.multiconnect.api.ProtocolBehavior;
import net.earthcomputer.multiconnect.protocols.v1_12.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.*;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.OptionalDouble;

public class Protocol_1_8 extends ProtocolBehavior {
    private static final Block[] BLOCKS_WITH_CHANGED_COLLISION = {
        Blocks.LADDER,
        Blocks.LILY_PAD,
    };

    public static final int LEVEL_EVENT_QUIET_GHAST_SHOOT = -1000 + 1;
    private static final EntityDimensions DEFAULT_BOAT_DIMENSIONS = EntityType.BOAT.getDimensions();

    @Override
    public void onSetup() {
        ((EntityTypeAccessor) EntityType.BOAT).setDimensions(EntityDimensions.scalable(1.5f, 0.5625f));
    }

    @Override
    public void onDisable() {
        ((EntityTypeAccessor) EntityType.BOAT).setDimensions(DEFAULT_BOAT_DIMENSIONS);
    }

    @Override
    public Block[] getBlocksWithChangedCollision() {
        return BLOCKS_WITH_CHANGED_COLLISION;
    }

    @Override
    public void onCommandRegistration(CommandRegistrationArgs args) {
        BrigadierRemover.of(args.dispatcher()).get("time").get("query").get("day").remove();
        BrigadierRemover.of(args.dispatcher()).get("scoreboard").get("players").get("tag").remove();
        BrigadierRemover.of(args.dispatcher()).get("scoreboard").get("teams").get("option").get("team").get("collisionRule").remove();
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
