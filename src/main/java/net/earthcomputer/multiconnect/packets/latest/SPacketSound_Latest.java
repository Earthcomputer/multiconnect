package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketSound;

@MessageVariant(minVersion = Protocols.V1_19)
public class SPacketSound_Latest implements SPacketSound {
    @Registry(Registries.SOUND_EVENT)
    public int soundId;
    public CommonTypes.SoundCategory category;
    @Type(Types.INT)
    public int positionX;
    @Type(Types.INT)
    public int positionY;
    @Type(Types.INT)
    public int positionZ;
    public float volume;
    public float pitch;
    @Type(Types.LONG)
    @Introduce(intValue = 0)
    public long seed;
}
