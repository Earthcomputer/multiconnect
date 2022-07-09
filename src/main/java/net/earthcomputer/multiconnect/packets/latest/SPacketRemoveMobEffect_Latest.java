package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketRemoveMobEffect;

@MessageVariant(minVersion = Protocols.V1_18_2)
public class SPacketRemoveMobEffect_Latest implements SPacketRemoveMobEffect {
    public int entityId;
    @Registry(Registries.MOB_EFFECT)
    public int effectId;
}
