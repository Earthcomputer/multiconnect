package net.earthcomputer.multiconnect.protocols.v1_13;

import net.earthcomputer.multiconnect.protocols.generic.TagRegistry;
import net.earthcomputer.multiconnect.protocols.v1_13_1.Protocol_1_13_1;
import net.minecraft.block.*;
import net.minecraft.tag.BlockTags;

public class Protocol_1_13 extends Protocol_1_13_1 {
    @Override
    public void addExtraBlockTags(TagRegistry<Block> tags) {
        tags.add(BlockTags.UNDERWATER_BONEMEALS, Blocks.SEAGRASS);
        tags.addTag(BlockTags.UNDERWATER_BONEMEALS, BlockTags.CORALS);
        tags.addTag(BlockTags.UNDERWATER_BONEMEALS, BlockTags.WALL_CORALS);
        tags.add(BlockTags.CORAL_PLANTS, Blocks.TUBE_CORAL, Blocks.BRAIN_CORAL, Blocks.BUBBLE_CORAL, Blocks.FIRE_CORAL, Blocks.HORN_CORAL);
        super.addExtraBlockTags(tags);
    }
}
