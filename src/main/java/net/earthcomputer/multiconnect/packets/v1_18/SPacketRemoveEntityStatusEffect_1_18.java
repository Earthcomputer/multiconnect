package net.earthcomputer.multiconnect.packets.v1_18;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketRemoveEntityStatusEffect;

@MessageVariant(maxVersion = Protocols.V1_18)
public class SPacketRemoveEntityStatusEffect_1_18 implements SPacketRemoveEntityStatusEffect {
    public int entityId;
    @Registry(Registries.MOB_EFFECT)
    public byte effectId;
}
