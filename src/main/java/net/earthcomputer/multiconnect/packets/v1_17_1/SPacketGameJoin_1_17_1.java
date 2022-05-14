package net.earthcomputer.multiconnect.packets.v1_17_1;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Datafix;
import net.earthcomputer.multiconnect.ap.DatafixTypes;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketGameJoin;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.List;

@MessageVariant(minVersion = Protocols.V1_16_2, maxVersion = Protocols.V1_17_1)
public class SPacketGameJoin_1_17_1 implements SPacketGameJoin {
    @Type(Types.INT)
    public int entityId;
    @Introduce(compute = "computeHardcore")
    public boolean isHardcore;
    @Type(Types.UNSIGNED_BYTE)
    @Introduce(compute = "computeGamemode")
    public int gamemode;
    public byte previousGamemode;
    public List<Identifier> dimensions;
    @Datafix(DatafixTypes.REGISTRY_MANAGER)
    public NbtCompound registryManager;
    @Datafix(DatafixTypes.DIMENSION)
    @Introduce(compute = "computeDimensionType")
    public NbtCompound dimensionType;
    public Identifier dimension;
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

    public static NbtCompound computeDimensionType(@Argument("dimensionType") Identifier dimension) {
        NbtCompound dimType = new NbtCompound();
        dimType.putString("name", dimension.toString());
        return dimType;
    }
}
