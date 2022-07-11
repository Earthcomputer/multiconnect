package net.earthcomputer.multiconnect.packets.v1_17_1;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Datafix;
import net.earthcomputer.multiconnect.ap.DatafixTypes;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketLogin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

@MessageVariant(minVersion = Protocols.V1_16_2, maxVersion = Protocols.V1_17_1)
public class SPacketLogin_1_17_1 implements SPacketLogin {
    @Type(Types.INT)
    public int entityId;
    @Introduce(compute = "computeHardcore")
    public boolean isHardcore;
    @Type(Types.UNSIGNED_BYTE)
    @Introduce(compute = "computeGamemode")
    public int gamemode;
    public byte previousGamemode;
    public List<ResourceLocation> dimensions;
    @Datafix(DatafixTypes.REGISTRY_ACCESS)
    public CompoundTag registryManager;
    @Datafix(DatafixTypes.DIMENSION)
    @Introduce(compute = "computeDimensionType")
    public CompoundTag dimensionType;
    public ResourceLocation dimension;
    @Type(Types.LONG)
    public long hashedSeed;
    public int maxPlayers;
    public int viewDistance;
    public boolean reducedDebugInfo;
    public boolean enableRespawnScreen;
    public boolean isDebug;
    public boolean isFlat;

    public static boolean computeHardcore(@Argument("gamemode") int gamemode) {
        return (gamemode & 8) == 8;
    }

    public static int computeGamemode(@Argument("gamemode") int gamemode) {
        return gamemode & ~8;
    }

    public static CompoundTag computeDimensionType(@Argument("dimensionType") ResourceLocation dimension) {
        CompoundTag dimType = new CompoundTag();
        dimType.putString("name", dimension.toString());
        return dimType;
    }
}
