package net.earthcomputer.multiconnect.protocols.v1_18;

import net.minecraft.client.multiplayer.ClientLevel;

// TODO: is this applicable on via?
public interface IBlockStatePredictionHandler {
    void multiconnect_nullifyServerVerifiedStatesUpTo(ClientLevel world, int sequence);
}
