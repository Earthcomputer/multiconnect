package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;

@MessageVariant
public class CPacketUpdateBeacon {
    @Registry(Registries.STATUS_EFFECT)
    public int primaryEffect;
    @Registry(Registries.STATUS_EFFECT)
    public int secondaryEffect;
}
