package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Datafix;
import net.earthcomputer.multiconnect.ap.DatafixTypes;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketEntityStatusEffect;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

@MessageVariant(minVersion = Protocols.V1_19)
public class SPacketEntityStatusEffect_Latest implements SPacketEntityStatusEffect {
    public int entityId;
    @Registry(Registries.STATUS_EFFECT)
    public int effectId;
    public byte amplifier;
    public int duration;
    public byte flags;
    @Introduce(defaultConstruct = true)
    @Datafix(DatafixTypes.STATUS_EFFECT_FACTOR_DATA)
    public Optional<NbtCompound> factorCalculationData;
}
