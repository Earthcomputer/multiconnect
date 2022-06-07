package net.earthcomputer.multiconnect.protocols.v1_18_2.mixin;

import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientWorld.class)
public interface ClientWorldAccessor {
    @Accessor("pendingUpdateManager")
    PendingUpdateManager multiconnect_getPendingUpdateManager();
}
