package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketEntityStatusEffect;

@MessageVariant(minVersion = Protocols.V1_18_2, maxVersion = Protocols.V1_18_2)
public class SPacketEntityStatusEffect_1_18_2 implements SPacketEntityStatusEffect {
    public int entityId;
    @Registry(Registries.MOB_EFFECT)
    public int effectId;
    public byte amplifier;
    public int duration;
    public byte flags;
}
