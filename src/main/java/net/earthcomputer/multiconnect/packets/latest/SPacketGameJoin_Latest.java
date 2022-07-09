package net.earthcomputer.multiconnect.packets.latest;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
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
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.datafix.MulticonnectDFU;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketGameJoin;
import net.earthcomputer.multiconnect.protocols.generic.DimensionTypeReference;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@MessageVariant(minVersion = Protocols.V1_19)
public class SPacketGameJoin_Latest implements SPacketGameJoin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Type(Types.INT)
    public int entityId;
    public boolean isHardcore;
    @Type(Types.UNSIGNED_BYTE)
    public int gamemode;
    public byte previousGamemode;
    public List<ResourceLocation> dimensions;
    @Datafix(DatafixTypes.REGISTRY_ACCESS)
    public CompoundTag registryManager;
    @Introduce(compute = "computeDimensionType")
    public ResourceLocation dimensionType;
    public ResourceLocation dimension;
    @Type(Types.LONG)
    public long hashedSeed;
    public int maxPlayers;
    public int viewDistance;
    public int simulationDistance;
    public boolean reducedDebugInfo;
    public boolean enableRespawnScreen;
    public boolean isDebug;
    public boolean isFlat;
    @Introduce(defaultConstruct = true)
    public Optional<CommonTypes.GlobalPos> lastDeathPos;

    public static ResourceLocation computeDimensionType(
            @Argument("registryManager") CompoundTag registryManager,
            @Argument("dimensionType") CompoundTag dimensionType,
            @Argument("dimension") ResourceLocation dimensionName
    ) {
        CompoundTag updatedDimType = (CompoundTag) MulticonnectDFU.FIXER.update(
                MulticonnectDFU.DIMENSION,
                new Dynamic<>(NbtOps.INSTANCE, dimensionType),
                ConnectionMode.byValue(ConnectionInfo.protocolVersion).getDataVersion(),
                ConnectionMode.V1_18_2.getDataVersion()
        ).getValue();
        CompoundTag updatedRegistryManager = (CompoundTag) MulticonnectDFU.FIXER.update(
                MulticonnectDFU.REGISTRY_ACCESS,
                new Dynamic<>(NbtOps.INSTANCE, registryManager),
                ConnectionMode.byValue(ConnectionInfo.protocolVersion).getDataVersion(),
                ConnectionMode.V1_18_2.getDataVersion()
        ).getValue();
        if (!updatedRegistryManager.contains("minecraft:dimension_type", Tag.TAG_COMPOUND)) {
            return Level.OVERWORLD.location();
        }
        CompoundTag dimensionTypes = updatedRegistryManager.getCompound("minecraft:dimension_type");
        if (!dimensionTypes.contains("value", Tag.TAG_LIST)) {
            return Level.OVERWORLD.location();
        }
        ListTag dimTypeValues = dimensionTypes.getList("value", Tag.TAG_COMPOUND);

        ResourceLocation matchedDimension = Level.OVERWORLD.location();
        int maxScore = -1;
        for (int i = 0; i < dimTypeValues.size(); i++) {
            CompoundTag dimension = dimTypeValues.getCompound(i);
            if (!dimension.contains("name", Tag.TAG_STRING) || !dimension.contains("element", Tag.TAG_COMPOUND)) {
                continue;
            }
            int score = matchDimensionType(dimension.getCompound("element"), updatedDimType);
            String name = dimension.getString("name");
            if (name.equals(dimensionName.toString())) {
                score += 90;
            }
            if (score > maxScore) {
                maxScore = score;
                matchedDimension = new ResourceLocation(name);
            }
        }
        if (maxScore < 0) {
            LOGGER.error("Failed to find a matching dimension type for {}", updatedDimType);
        }
        return matchedDimension;
    }

    public static int matchDimensionType(CompoundTag a, CompoundTag b) {
        // match height first, it's the most important
        if (a.getInt("height") != b.getInt("height")) {
            return -101;
        }
        if (a.getInt("min_y") != b.getInt("min_y")) {
            return -101;
        }

        int score = 0;

        // visual effects have a high score, but don't need to disconnect the client if they don't match
        if (a.getBoolean("has_skylight") == b.getBoolean("has_skylight")) {
            score += 100;
        }
        score += (int) (Math.abs(a.getFloat("ambient_light") - b.getFloat("ambient_light")) * 100);

        if (a.getString("effects").equals(b.getString("effects"))) {
            score += 100;
        }

        // some nice-to-haves
        if (a.getBoolean("has_ceiling") == b.getBoolean("has_ceiling")) {
            score += 20;
        }
        if (a.getString("infiniburn").equals(b.getString("infiniburn"))) {
            score += 20;
        }

        return score;
    }

    @PartialHandler
    public static void saveRegistryManager(
            @Argument("registryManager") CompoundTag registryManager,
            @Argument("dimensionType") ResourceLocation dimensionType,
            @GlobalData Consumer<RegistryAccess> registryManagerSetter,
            @GlobalData Consumer<DimensionTypeReference> dimensionTypeSetter
    ) {
        {
            Dynamic<?> updated = MulticonnectDFU.FIXER.update(
                    MulticonnectDFU.REGISTRY_ACCESS,
                    new Dynamic<>(NbtOps.INSTANCE, registryManager),
                    ConnectionMode.byValue(ConnectionInfo.protocolVersion).getDataVersion(),
                    SharedConstants.getCurrentVersion().getDataVersion().getVersion()
            );
            var dataResult = RegistryAccess.NETWORK_CODEC.decode(updated);
            RegistryAccess result = dataResult.getOrThrow(false, err -> {}).getFirst();
            registryManagerSetter.accept(result);
        }
        {
            dimensionTypeSetter.accept(new DimensionTypeReference(dimensionType));
        }
    }
}
