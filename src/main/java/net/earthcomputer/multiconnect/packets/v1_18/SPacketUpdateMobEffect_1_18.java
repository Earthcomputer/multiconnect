package net.earthcomputer.multiconnect.packets.v1_18;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketUpdateMobEffect;

@MessageVariant(maxVersion = Protocols.V1_18)
public class SPacketUpdateMobEffect_1_18 implements SPacketUpdateMobEffect {
    public int entityId;
    @Registry(Registries.MOB_EFFECT)
    public byte effectId;
    public byte amplifier;
    public int duration;
    public byte flags;
}
