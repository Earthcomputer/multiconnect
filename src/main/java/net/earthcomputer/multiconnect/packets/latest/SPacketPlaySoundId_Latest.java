package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketPlaySoundId;
import net.minecraft.util.Identifier;

@MessageVariant(minVersion = Protocols.V1_19)
public class SPacketPlaySoundId_Latest implements SPacketPlaySoundId {
    @Registry(Registries.SOUND_EVENT)
    public Identifier id;
    public CommonTypes.SoundCategory category;
    @Type(Types.INT)
    public int x;
    @Type(Types.INT)
    public int y;
    @Type(Types.INT)
    public int z;
    public float volume;
    public float pitch;
    @Type(Types.LONG)
    @Introduce(intValue = 0)
    public long seed;
}
