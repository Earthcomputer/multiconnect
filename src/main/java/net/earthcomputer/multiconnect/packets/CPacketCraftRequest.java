package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.minecraft.util.Identifier;

@MessageVariant
public class CPacketCraftRequest {
    public byte syncId;
    public Identifier recipeId;
    public boolean craftAll;
}
