package net.earthcomputer.multiconnect.packets.v1_18;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketRemoveMobEffect;

@MessageVariant(maxVersion = Protocols.V1_18)
public class SPacketRemoveMobEffect_1_18 implements SPacketRemoveMobEffect {
    public int entityId;
    @Registry(Registries.MOB_EFFECT)
    public byte effectId;
}
