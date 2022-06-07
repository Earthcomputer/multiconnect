package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketPlaySoundId;

@MessageVariant(maxVersion = Protocols.V1_12_2)
public class SPacketPlaySoundId_1_12_2 implements SPacketPlaySoundId {
    public String id;
    public CommonTypes.SoundCategory category;
    @Type(Types.INT)
    public int x;
    @Type(Types.INT)
    public int y;
    @Type(Types.INT)
    public int z;
    public float volume;
    public float pitch;
}
