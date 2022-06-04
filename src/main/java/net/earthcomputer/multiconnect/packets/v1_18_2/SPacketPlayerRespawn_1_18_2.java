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
import net.earthcomputer.multiconnect.packets.SPacketPlayerRespawn;
import net.earthcomputer.multiconnect.protocols.v1_18_2.DiggingTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

@MessageVariant(minVersion = Protocols.V1_16_2, maxVersion = Protocols.V1_18_2)
public class SPacketPlayerRespawn_1_18_2 implements SPacketPlayerRespawn {
    @Datafix(DatafixTypes.DIMENSION)
    @Introduce(compute = "computeDimension")
    public NbtCompound dimension;
    public Identifier dimensionId;
    @Type(Types.LONG)
    public long hashedSeed;
    @Type(Types.UNSIGNED_BYTE)
    public int gamemode;
    @Type(Types.UNSIGNED_BYTE)
    public int previousGamemode;
    public boolean isDebug;
    public boolean isFlat;
    public boolean copyMetadata;

    public static NbtCompound computeDimension(@Argument("dimension") Identifier dimension) {
        NbtCompound dimType = new NbtCompound();
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
