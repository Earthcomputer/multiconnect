package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.minecraft.util.Identifier;

@Message
public class SPacketCraftFailedResponse {
    public byte syncId;
    public Identifier recipe;
}
