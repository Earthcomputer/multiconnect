package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;

@MessageVariant
public class SPacketCooldownUpdate {
    @Registry(Registries.ITEM)
    public int itemId;
    public int cooldownTicks;
}
