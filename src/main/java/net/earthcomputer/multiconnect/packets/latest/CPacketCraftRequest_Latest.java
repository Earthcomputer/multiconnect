package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketCraftRequest;
import net.minecraft.util.Identifier;

@MessageVariant(minVersion = Protocols.V1_13)
public class CPacketCraftRequest_Latest implements CPacketCraftRequest {
    public byte syncId;
    public Identifier recipeId;
    public boolean craftAll;
}
