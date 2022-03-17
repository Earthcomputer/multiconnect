package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.minecraft.util.Identifier;

@MessageVariant
public class SPacketCraftFailedResponse {
    public byte syncId;
    public Identifier recipe;
}
