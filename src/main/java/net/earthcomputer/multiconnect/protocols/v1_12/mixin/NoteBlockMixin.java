package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12.NoteBlockBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(NoteBlock.class)
public class NoteBlockMixin extends Block implements EntityBlock {

    public NoteBlockMixin(Properties settings) {
        super(settings);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ConnectionInfo.protocolVersion <= Protocols.V1_12_2 ? new NoteBlockBlockEntity(pos, state) : null;
    }

    @ModifyVariable(method = "triggerEvent", ordinal = 0, at = @At("HEAD"), argsOnly = true)
    private BlockState onTriggerEvent(BlockState localState, BlockState state, Level world, BlockPos pos, int type, int data) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_12_2)
            return localState;
        NoteBlockInstrument instrument = type < 0 || type >= 10 ? NoteBlockInstrument.HARP : NoteBlockInstrument.values()[type];
        state = state.setValue(NoteBlock.INSTRUMENT, instrument).setValue(NoteBlock.NOTE, Mth.clamp(data, 0, 24));
        world.setBlock(pos, state, 18);
        return state;
    }

}
