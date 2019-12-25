package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;

public class NoteBlock_1_12_2 extends NoteBlock implements BlockEntityProvider {

    public NoteBlock_1_12_2(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new NoteBlockBlockEntity();
    }
}
