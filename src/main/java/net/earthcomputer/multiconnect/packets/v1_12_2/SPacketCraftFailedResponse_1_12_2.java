package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketCraftFailedResponse;

@MessageVariant(maxVersion = Protocols.V1_12_2)
public class SPacketCraftFailedResponse_1_12_2 implements SPacketCraftFailedResponse {
    public byte syncId;
    public int recipe;
}
