package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;

@Message
public class SPacketCooldownUpdate {
    @Registry(Registries.ITEM)
    public int itemId;
    public int cooldownTicks;
}
