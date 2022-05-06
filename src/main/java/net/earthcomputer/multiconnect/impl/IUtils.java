package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.protocols.generic.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;

public interface IUtils {
    default void copyBlocks(TagRegistry<Item> tags, TagRegistry<Block> blockTags, Tag.Identified<Item> tag, Tag.Identified<Block> blockTag) {
        Utils.copyBlocks(tags, blockTags, tag, blockTag);
    }
}
