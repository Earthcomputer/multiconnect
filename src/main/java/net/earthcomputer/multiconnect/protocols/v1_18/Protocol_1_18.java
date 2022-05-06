package net.earthcomputer.multiconnect.protocols.v1_18;

import net.earthcomputer.multiconnect.protocols.generic.TagRegistry;
import net.earthcomputer.multiconnect.protocols.v1_18_2.Protocol_1_18_2;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.tag.BlockTags;

public class Protocol_1_18 extends Protocol_1_18_2 {
    @Override
    public void addExtraBlockTags(TagRegistry<Block> tags) {
        tags.addTag(BlockTags.FALL_DAMAGE_RESETTING, BlockTags.CLIMBABLE);
        tags.add(BlockTags.FALL_DAMAGE_RESETTING, Blocks.SWEET_BERRY_BUSH, Blocks.COBWEB);
        super.addExtraBlockTags(tags);
    }
}
