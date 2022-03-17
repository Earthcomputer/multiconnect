package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.minecraft.util.Identifier;

@MessageVariant
public class SPacketPlaySoundId {
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
}
