package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.minecraft.util.Identifier;

@Message
public class SPacketStopSound {
    public byte flags;
    @OnlyIf(field = "flags", condition = "hasCategory")
    public CommonTypes.SoundCategory category;
    @OnlyIf(field = "flags", condition = "hasSound")
    @Registry(Registries.SOUND_EVENT)
    public Identifier sound;

    public static boolean hasCategory(byte flags) {
        return (flags & 1) != 0;
    }

    public static boolean hasSound(byte flags) {
        return (flags & 2) != 0;
    }
}
