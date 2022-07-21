package net.earthcomputer.multiconnect.packets;

import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketRemoveEntities {
    public IntList entityIds;
}
