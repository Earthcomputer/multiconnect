package net.earthcomputer.multiconnect.packets.v1_16_1;

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
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@MessageVariant(minVersion = Protocols.V1_16, maxVersion = Protocols.V1_16_1)
public class SPacketLogin_1_16_1 implements SPacketLogin {
    @Type(Types.INT)
    public int entityId;
    @Type(Types.UNSIGNED_BYTE)
    public int gamemode;
    // not_set game mode, unsigned -1 gets cast back to signed in 1.16.1 when
    // https://bugs.mojang.com/browse/MC-189885 was fixed
    @Type(Types.UNSIGNED_BYTE)
    @Introduce(intValue = 255)
    public int previousGamemode;
    @Introduce(compute = "computeDimensions")
    public List<ResourceLocation> dimensions;
    @Datafix(DatafixTypes.REGISTRY_ACCESS)
    @Introduce(compute = "computeRegistryManager")
    public CompoundTag registryManager;
    @Introduce(compute = "computeDimension")
    public ResourceLocation dimensionType;
    @Introduce(compute = "computeDimension")
    public ResourceLocation dimension;
    @Type(Types.LONG)
    public long hashedSeed;
    @Type(Types.UNSIGNED_BYTE)
    public int maxPlayers;
    public int viewDistance;
    public boolean reducedDebugInfo;
    public boolean enableRespawnScreen;
    @Introduce(compute = "computeIsDebug")
    public boolean isDebug;
    @Introduce(compute = "computeIsFlat")
    public boolean isFlat;

    public static List<ResourceLocation> computeDimensions() {
        List<ResourceLocation> list = new ArrayList<>(3);
        Collections.addAll(list, Level.OVERWORLD.location(), Level.NETHER.location(), Level.END.location());
        return list;
    }

    public static CompoundTag computeRegistryManager() {
        return new CompoundTag();
    }

    public static ResourceLocation computeDimension(@Argument("dimensionId") int dimensionId) {
        return switch (dimensionId) {
            case -1 -> Level.NETHER.location();
            case 1 -> Level.END.location();
            default -> Level.OVERWORLD.location();
        };
    }

    public static boolean computeIsDebug(@Argument("genType") String genType) {
        return "debug_all_block_states".equalsIgnoreCase(genType);
    }

    public static boolean computeIsFlat(@Argument("genType") String genType) {
        return "flat".equalsIgnoreCase(genType);
    }
}
