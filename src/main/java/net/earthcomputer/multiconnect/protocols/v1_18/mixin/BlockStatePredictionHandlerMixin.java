package net.earthcomputer.multiconnect.protocols.v1_18.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.earthcomputer.multiconnect.protocols.v1_18.IBlockStatePredictionHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockStatePredictionHandler.class)
public class BlockStatePredictionHandlerMixin implements IBlockStatePredictionHandler {
    @Shadow @Final private Long2ObjectOpenHashMap<BlockStatePredictionHandler.ServerVerifiedState> serverVerifiedStates;

    @Override
    public void multiconnect_nullifyServerVerifiedStatesUpTo(ClientLevel world, int sequence) {
        for (var entry : serverVerifiedStates.long2ObjectEntrySet()) {
            if (entry.getValue().sequence <= sequence) {
                entry.getValue().setBlockState(world.getBlockState(BlockPos.of(entry.getLongKey())));
            }
        }
    }
}
