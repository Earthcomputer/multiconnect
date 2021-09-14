package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class CPacketRecipeCategoryOptions {
    public Category category;
    public boolean bookOpen;
    public boolean filterActive;

    public enum Category {
        CRAFTING, FURNACE, BLAST_FURNACE, SMOKER
    }
}
