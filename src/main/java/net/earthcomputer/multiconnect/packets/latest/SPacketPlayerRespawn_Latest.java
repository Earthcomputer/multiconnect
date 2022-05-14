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
import net.earthcomputer.multiconnect.packets.SPacketPlayerRespawn;
import net.earthcomputer.multiconnect.protocols.generic.DimensionTypeReference;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.dimension.DimensionType;

import java.util.function.Consumer;

@MessageVariant(minVersion = Protocols.V1_16_2)
public class SPacketPlayerRespawn_Latest implements SPacketPlayerRespawn {
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
    public static void saveDimension(
            @Argument("dimension") NbtCompound dimension,
            @GlobalData Consumer<DimensionTypeReference> dimensionSetter
    ) {
        Dynamic<?> updated = MulticonnectDFU.FIXER.update(
                MulticonnectDFU.DIMENSION,
                new Dynamic<>(NbtOps.INSTANCE, dimension),
                ConnectionMode.byValue(ConnectionInfo.protocolVersion).getDataVersion(),
                SharedConstants.getGameVersion().getSaveVersion().getId()
        );
        var dataResult = DimensionType.REGISTRY_CODEC.decode(updated);
        RegistryEntry<DimensionType> result = dataResult.getOrThrow(false, err -> {}).getFirst();
        dimensionSetter.accept(new DimensionTypeReference(result));
    }
}
