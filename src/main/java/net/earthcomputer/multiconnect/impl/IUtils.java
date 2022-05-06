package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.protocols.generic.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;

public interface IUtils {
    default void copyBlocks(TagRegistry<Item> tags, TagRegistry<Block> blockTags, TagKey<Item> tag, TagKey<Block> blockTag) {
        Utils.copyBlocks(tags, blockTags, tag, blockTag);
    }
}
