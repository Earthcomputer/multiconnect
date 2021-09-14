package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.minecraft.util.Identifier;

@Message
public class CPacketCraftRequest {
    public byte syncId;
    public Identifier recipeId;
    public boolean craftAll;
}
