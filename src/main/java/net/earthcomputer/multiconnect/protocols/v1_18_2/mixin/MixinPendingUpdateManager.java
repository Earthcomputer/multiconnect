package net.earthcomputer.multiconnect.protocols.v1_18_2.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.earthcomputer.multiconnect.protocols.v1_18_2.IPendingUpdateManager;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PendingUpdateManager.class)
public class MixinPendingUpdateManager implements IPendingUpdateManager {
    @Shadow @Final private Long2ObjectOpenHashMap<PendingUpdateManager.PendingUpdate> blockPosToPendingUpdate;

    @Override
    public void multiconnect_nullifyPendingUpdatesUpTo(ClientWorld world, int sequence) {
        for (var entry : blockPosToPendingUpdate.long2ObjectEntrySet()) {
            if (entry.getValue().sequence <= sequence) {
                entry.getValue().setBlockState(world.getBlockState(BlockPos.fromLong(entry.getLongKey())));
            }
        }
    }
}
