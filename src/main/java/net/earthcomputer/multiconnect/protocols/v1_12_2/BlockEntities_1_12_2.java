package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.protocols.AbstractProtocol;
import net.minecraft.block.entity.BlockEntityType;

public class BlockEntities_1_12_2 {

    public static final BlockEntityType<NoteBlockBlockEntity> NOTE_BLOCK = BlockEntityType.Builder.create(NoteBlockBlockEntity::new, Blocks_1_12_2.NOTE_BLOCK).build(null);
    public static final BlockEntityType<FlowerPotBlockEntity> FLOWER_POT = BlockEntityType.Builder.create(FlowerPotBlockEntity::new,
            Blocks_1_12_2.FLOWER_POT,
            Blocks_1_12_2.POTTED_OAK_SAPLING,
            Blocks_1_12_2.POTTED_SPRUCE_SAPLING,
            Blocks_1_12_2.POTTED_BIRCH_SAPLING,
            Blocks_1_12_2.POTTED_JUNGLE_SAPLING,
            Blocks_1_12_2.POTTED_ACACIA_SAPLING,
            Blocks_1_12_2.POTTED_DARK_OAK_SAPLING,
            Blocks_1_12_2.POTTED_FERN,
            Blocks_1_12_2.POTTED_DANDELION,
            Blocks_1_12_2.POTTED_POPPY,
            Blocks_1_12_2.POTTED_BLUE_ORCHID,
            Blocks_1_12_2.POTTED_ALLIUM,
            Blocks_1_12_2.POTTED_AZURE_BLUET,
            Blocks_1_12_2.POTTED_RED_TULIP,
            Blocks_1_12_2.POTTED_ORANGE_TULIP,
            Blocks_1_12_2.POTTED_WHITE_TULIP,
            Blocks_1_12_2.POTTED_PINK_TULIP,
            Blocks_1_12_2.POTTED_OXEYE_DAISY,
            Blocks_1_12_2.POTTED_RED_MUSHROOM,
            Blocks_1_12_2.POTTED_BROWN_MUSHROOM,
            Blocks_1_12_2.POTTED_DEAD_BUSH,
            Blocks_1_12_2.POTTED_CACTUS
    ).build(null);

    public static void registerBlockEntities(ISimpleRegistry<BlockEntityType<?>> registry) {
        registry.unregister(BlockEntityType.CONDUIT);
        AbstractProtocol.insertAfter(registry, BlockEntityType.MOB_SPAWNER, NOTE_BLOCK, "noteblock");
        AbstractProtocol.insertAfter(registry, BlockEntityType.COMPARATOR, FLOWER_POT, "flower_pot");
    }

}
