package net.earthcomputer.multiconnect.protocols.v1_18_2;

import net.minecraft.client.world.ClientWorld;

public interface IPendingUpdateManager {
    void multiconnect_nullifyPendingUpdatesUpTo(ClientWorld world, int sequence);
}
