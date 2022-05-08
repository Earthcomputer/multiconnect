package net.earthcomputer.multiconnect.packets.latest;

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
import net.earthcomputer.multiconnect.packets.SPacketGameJoin;
import net.earthcomputer.multiconnect.protocols.generic.DimensionTypeReference;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.dimension.DimensionType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@MessageVariant(minVersion = Protocols.V1_18)
public class SPacketGameJoin_Latest implements SPacketGameJoin {
    @Type(Types.INT)
    public int entityId;
    public boolean isHardcore;
    @Type(Types.UNSIGNED_BYTE)
    public int gamemode;
    public byte previousGamemode;
    public List<Identifier> dimensions;
    @Datafix(DatafixTypes.REGISTRY_MANAGER)
    public NbtCompound registryManager;
    @Datafix(DatafixTypes.DIMENSION)
    public NbtCompound dimensionType;
    public Identifier dimension;
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
    public static void saveRegistryManager(
            @Argument("registryManager") NbtCompound registryManager,
            @Argument("dimensionType") NbtCompound dimensionType,
            @GlobalData Consumer<DynamicRegistryManager> registryManagerSetter,
            @GlobalData Consumer<DimensionTypeReference> dimensionTypeSetter
    ) {
        {
            Dynamic<?> updated = MulticonnectDFU.FIXER.update(
                    MulticonnectDFU.REGISTRY_MANAGER,
                    new Dynamic<>(NbtOps.INSTANCE, registryManager),
                    ConnectionMode.byValue(ConnectionInfo.protocolVersion).getDataVersion(),
                    SharedConstants.getGameVersion().getSaveVersion().getId()
            );
            try {
                NbtIo.writeCompressed(((NbtCompound) updated.convert(NbtOps.INSTANCE).getValue()), new File("temp" +
                            ".nbt"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            var dataResult = DynamicRegistryManager.CODEC.decode(updated);
            DynamicRegistryManager result = dataResult.getOrThrow(false, err -> {}).getFirst();
            registryManagerSetter.accept(result);
        }
        {
            Dynamic<?> updated = MulticonnectDFU.FIXER.update(
                    MulticonnectDFU.DIMENSION,
                    new Dynamic<>(NbtOps.INSTANCE, dimensionType),
                    ConnectionMode.byValue(ConnectionInfo.protocolVersion).getDataVersion(),
                    SharedConstants.getGameVersion().getSaveVersion().getId()
            );
            var dataResult = DimensionType.REGISTRY_CODEC.decode(updated);
            RegistryEntry<DimensionType> result = dataResult.getOrThrow(false, err -> {}).getFirst();
            dimensionTypeSetter.accept(new DimensionTypeReference(result));
        }
    }
}
