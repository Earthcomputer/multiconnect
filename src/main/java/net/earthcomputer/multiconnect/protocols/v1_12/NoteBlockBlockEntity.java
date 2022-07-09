package net.earthcomputer.multiconnect.protocols.v1_12;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class NoteBlockBlockEntity extends BlockEntity {

    public NoteBlockBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities_1_12_2.NOTE_BLOCK, pos, state);
    }
}
