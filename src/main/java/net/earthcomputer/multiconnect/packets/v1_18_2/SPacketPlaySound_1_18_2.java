package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketPlaySound;

@MessageVariant(maxVersion = Protocols.V1_18_2)
public class SPacketPlaySound_1_18_2 implements SPacketPlaySound {
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
}
