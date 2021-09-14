package net.earthcomputer.multiconnect.packets;

import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketEntitiesDestroy {
    public IntList entityIds;
}
