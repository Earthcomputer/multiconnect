package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.protocols.AbstractProtocol;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;

public class BlockEntities_1_12_2 {

    public static final BlockEntityType<NoteBlockBlockEntity> NOTE_BLOCK = BlockEntityType.Builder.create(NoteBlockBlockEntity::new, Blocks.NOTE_BLOCK).build(null);
    public static final BlockEntityType<FlowerPotBlockEntity> FLOWER_POT = BlockEntityType.Builder.create(FlowerPotBlockEntity::new,
            Blocks.FLOWER_POT,
            Blocks.POTTED_OAK_SAPLING,
            Blocks.POTTED_SPRUCE_SAPLING,
            Blocks.POTTED_BIRCH_SAPLING,
            Blocks.POTTED_JUNGLE_SAPLING,
            Blocks.POTTED_ACACIA_SAPLING,
            Blocks.POTTED_DARK_OAK_SAPLING,
            Blocks.POTTED_FERN,
            Blocks.POTTED_DANDELION,
            Blocks.POTTED_POPPY,
            Blocks.POTTED_BLUE_ORCHID,
            Blocks.POTTED_ALLIUM,
            Blocks.POTTED_AZURE_BLUET,
            Blocks.POTTED_RED_TULIP,
            Blocks.POTTED_ORANGE_TULIP,
            Blocks.POTTED_WHITE_TULIP,
            Blocks.POTTED_PINK_TULIP,
            Blocks.POTTED_OXEYE_DAISY,
            Blocks.POTTED_RED_MUSHROOM,
            Blocks.POTTED_BROWN_MUSHROOM,
            Blocks.POTTED_DEAD_BUSH,
            Blocks.POTTED_CACTUS
    ).build(null);

    public static void registerBlockEntities(ISimpleRegistry<BlockEntityType<?>> registry) {
        registry.unregister(BlockEntityType.CONDUIT);
        AbstractProtocol.insertAfter(registry, BlockEntityType.MOB_SPAWNER, NOTE_BLOCK, "noteblock");
        AbstractProtocol.insertAfter(registry, BlockEntityType.COMPARATOR, FLOWER_POT, "flower_pot");
    }

}
