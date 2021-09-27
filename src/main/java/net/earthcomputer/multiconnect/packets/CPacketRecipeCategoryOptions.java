package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.NetworkEnum;

@Message
public class CPacketRecipeCategoryOptions {
    public Category category;
    public boolean bookOpen;
    public boolean filterActive;

    @NetworkEnum
    public enum Category {
        CRAFTING, FURNACE, BLAST_FURNACE, SMOKER
    }
}
