package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;

@Message
public class SPacketRemoveEntityStatusEffect {
    public int entityId;
    @Registry(Registries.STATUS_EFFECT)
    public byte effectId;
}
