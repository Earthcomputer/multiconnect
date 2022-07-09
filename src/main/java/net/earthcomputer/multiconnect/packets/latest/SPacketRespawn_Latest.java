package net.earthcomputer.multiconnect.packets.latest;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import net.earthcomputer.multiconnect.ap.Argument;
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
import net.earthcomputer.multiconnect.packets.SPacketRespawn;
import net.earthcomputer.multiconnect.protocols.generic.DimensionTypeReference;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.Consumer;

@MessageVariant(minVersion = Protocols.V1_19)
public class SPacketRespawn_Latest implements SPacketRespawn {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Introduce(compute = "computeDimension")
    public ResourceLocation dimension;
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
    @Introduce(defaultConstruct = true)
    public Optional<CommonTypes.GlobalPos> lastDeathPos;

    public static ResourceLocation computeDimension(
            @Argument("dimension") CompoundTag dimension,
            @Argument("dimensionId") ResourceLocation dimensionId,
            @GlobalData RegistryAccess registryManager
    ) {
        CompoundTag updatedDimension = (CompoundTag) MulticonnectDFU.FIXER.update(
                MulticonnectDFU.DIMENSION,
                new Dynamic<>(NbtOps.INSTANCE, dimension),
                ConnectionMode.byValue(ConnectionInfo.protocolVersion).getDataVersion(),
                SharedConstants.getCurrentVersion().getDataVersion().getVersion()
        ).getValue();
        updatedDimension = (CompoundTag) DimensionType.DIRECT_CODEC.encodeStart(
                NbtOps.INSTANCE,
                DimensionType.DIRECT_CODEC.decode(
                        new Dynamic<>(NbtOps.INSTANCE, updatedDimension)
                ).result().orElseThrow().getFirst()
        ).result().orElseThrow();
        Registry<DimensionType> dimensionTypeRegistry = registryManager.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);

        ResourceLocation matchedDimension = Level.OVERWORLD.location();
        int maxScore = -1;

        for (DimensionType dimType : dimensionTypeRegistry) {
            CompoundTag candidate = (CompoundTag) DimensionType.DIRECT_CODEC.encodeStart(NbtOps.INSTANCE, dimType).result().orElseThrow();
            int score = SPacketLogin_Latest.matchDimensionType(candidate, updatedDimension);
            ResourceLocation candidateId = dimensionTypeRegistry.getKey(dimType);
            if (dimensionId.equals(candidateId)) {
                score += 90;
            }
            if (score > maxScore) {
                maxScore = score;
                matchedDimension = candidateId;
            }
        }
        if (maxScore < 0) {
            LOGGER.error("Failed to find a matching dimension type for {}", dimensionId);
        }
        return matchedDimension;
    }

    @PartialHandler
    public static void saveDimension(
            @Argument("dimension") ResourceLocation dimension,
            @GlobalData Consumer<DimensionTypeReference> dimensionSetter
    ) {
        dimensionSetter.accept(new DimensionTypeReference(dimension));
    }
}
