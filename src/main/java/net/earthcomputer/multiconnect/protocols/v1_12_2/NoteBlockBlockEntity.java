package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class NoteBlockBlockEntity extends BlockEntity {

    public NoteBlockBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities_1_12_2.NOTE_BLOCK, pos, state);
    }
}
