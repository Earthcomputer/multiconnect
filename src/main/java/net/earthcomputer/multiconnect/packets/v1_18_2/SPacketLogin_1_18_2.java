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
import net.earthcomputer.multiconnect.packets.SPacketLogin;
import net.earthcomputer.multiconnect.protocols.v1_18.DiggingTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.function.Consumer;

@MessageVariant(minVersion = Protocols.V1_18, maxVersion = Protocols.V1_18_2)
public class SPacketLogin_1_18_2 implements SPacketLogin {
    @Type(Types.INT)
    public int entityId;
    public boolean isHardcore;
    @Type(Types.UNSIGNED_BYTE)
    public int gamemode;
    public byte previousGamemode;
    public List<ResourceLocation> dimensions;
    @Datafix(DatafixTypes.REGISTRY_ACCESS)
    public CompoundTag registryAccess;
    @Datafix(DatafixTypes.DIMENSION)
    public CompoundTag dimensionType;
    public ResourceLocation dimension;
    @Type(Types.LONG)
    public long hashedSeed;
    public int maxPlayers;
    public int viewDistance;
    @Introduce(compute = "computeSimulationDistance")
    public int simulationDistance;
    public boolean reducedDebugInfo;
    public boolean enableRespawnScreen;
    public boolean isDebug;
    public boolean isFlat;

    public static int computeSimulationDistance(
            @Argument("viewDistance") int viewDistance
    ) {
        return viewDistance;
    }

    @PartialHandler
    public static void createDiggingTracker(
            @GlobalData Consumer<DiggingTracker> diggingTrackerSetter
    ) {
        diggingTrackerSetter.accept(DiggingTracker.create());
    }
}
