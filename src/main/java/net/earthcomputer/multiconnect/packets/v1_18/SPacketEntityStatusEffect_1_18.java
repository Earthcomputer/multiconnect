package net.earthcomputer.multiconnect.packets.v1_18;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketEntityStatusEffect;

@MessageVariant(maxVersion = Protocols.V1_18)
public class SPacketEntityStatusEffect_1_18 implements SPacketEntityStatusEffect {
    public int entityId;
    @Registry(Registries.STATUS_EFFECT)
    public byte effectId;
    public byte amplifier;
    public int duration;
    public byte flags;
}
