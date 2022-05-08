package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketRemoveEntityStatusEffect;

@MessageVariant(minVersion = Protocols.V1_18_2)
public class SPacketRemoveEntityStatusEffect_Latest implements SPacketRemoveEntityStatusEffect {
    public int entityId;
    @Registry(Registries.STATUS_EFFECT)
    public int effectId;
}
