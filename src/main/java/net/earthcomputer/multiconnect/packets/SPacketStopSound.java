package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.minecraft.resources.ResourceLocation;

@MessageVariant
public class SPacketStopSound {
    public byte flags;
    @OnlyIf("hasCategory")
    public CommonTypes.SoundCategory category;
    @OnlyIf("hasSound")
    @Registry(Registries.SOUND_EVENT)
    public ResourceLocation sound;

    public static boolean hasCategory(@Argument("flags") byte flags) {
        return (flags & 1) != 0;
    }

    public static boolean hasSound(@Argument("flags") byte flags) {
        return (flags & 2) != 0;
    }
}
