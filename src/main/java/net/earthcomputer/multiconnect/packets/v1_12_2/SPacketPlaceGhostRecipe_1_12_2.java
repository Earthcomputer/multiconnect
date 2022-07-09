package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketPlaceGhostRecipe;

@MessageVariant(minVersion = Protocols.V1_12_1, maxVersion = Protocols.V1_12_2)
public class SPacketPlaceGhostRecipe_1_12_2 implements SPacketPlaceGhostRecipe {
    public byte syncId;
    public int recipe;
}
