package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketSoundEntity;

@MessageVariant(minVersion = Protocols.V1_14, maxVersion = Protocols.V1_18_2)
public class SPacketSoundEntity_1_18_2 implements SPacketSoundEntity {
    @Registry(Registries.SOUND_EVENT)
    public int soundId;
    public CommonTypes.SoundCategory category;
    public int entityId;
    public float volume;
    public float pitch;
}
