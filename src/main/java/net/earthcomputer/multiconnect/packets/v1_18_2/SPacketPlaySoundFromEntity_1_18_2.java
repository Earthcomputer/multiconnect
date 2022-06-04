package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketPlaySoundFromEntity;

@MessageVariant(minVersion = Protocols.V1_14, maxVersion = Protocols.V1_18_2)
public class SPacketPlaySoundFromEntity_1_18_2 implements SPacketPlaySoundFromEntity {
    @Registry(Registries.SOUND_EVENT)
    public int soundId;
    public CommonTypes.SoundCategory category;
    public int entityId;
    public float volume;
    public float pitch;
}
