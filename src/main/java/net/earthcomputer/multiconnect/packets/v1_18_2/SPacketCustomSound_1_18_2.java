package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketCustomSound;
import net.minecraft.resources.ResourceLocation;

@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
public class SPacketCustomSound_1_18_2 implements SPacketCustomSound {
    @Registry(Registries.SOUND_EVENT)
    @Introduce(compute = "computeId")
    public ResourceLocation id;
    public CommonTypes.SoundCategory category;
    @Type(Types.INT)
    public int x;
    @Type(Types.INT)
    public int y;
    @Type(Types.INT)
    public int z;
    public float volume;
    public float pitch;

    public static ResourceLocation computeId(@Argument("id") String id) {
        ResourceLocation newId = ResourceLocation.tryParse(id);
        return newId == null ? new ResourceLocation("ambient.cave") : newId;
    }
}
