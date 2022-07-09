package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Datafix;
import net.earthcomputer.multiconnect.ap.DatafixTypes;
import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.PartialHandler;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketRespawn;
import net.earthcomputer.multiconnect.protocols.v1_18.DiggingTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Consumer;

@MessageVariant(minVersion = Protocols.V1_16_2, maxVersion = Protocols.V1_18_2)
public class SPacketRespawn_1_18_2 implements SPacketRespawn {
    @Datafix(DatafixTypes.DIMENSION)
    @Introduce(compute = "computeDimension")
    public CompoundTag dimension;
    public ResourceLocation dimensionId;
    @Type(Types.LONG)
    public long hashedSeed;
    @Type(Types.UNSIGNED_BYTE)
    public int gamemode;
    @Type(Types.UNSIGNED_BYTE)
    public int previousGamemode;
    public boolean isDebug;
    public boolean isFlat;
    public boolean copyMetadata;

    public static CompoundTag computeDimension(@Argument("dimension") ResourceLocation dimension) {
        CompoundTag dimType = new CompoundTag();
        dimType.putString("name", dimension.toString());
        return dimType;
    }

    @PartialHandler
    public static void replaceDiggingTracker(
            @GlobalData Consumer<DiggingTracker> diggingTrackerSetter
    ) {
        diggingTrackerSetter.accept(DiggingTracker.create());
    }
}
