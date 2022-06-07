package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Datafix;
import net.earthcomputer.multiconnect.ap.DatafixTypes;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketBlockEntityUpdate;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

@MessageVariant(minVersion = Protocols.V1_18)
public class SPacketBlockEntityUpdate_Latest implements SPacketBlockEntityUpdate {
    public CommonTypes.BlockPos pos;
    @Registry(Registries.BLOCK_ENTITY_TYPE)
    @Introduce(compute = "computeBlockEntityType")
    public int blockEntityType;
    @Datafix(value = DatafixTypes.BLOCK_ENTITY, preprocess = "preprocessBlockEntityData")
    public NbtCompound data;

    public static int computeBlockEntityType(
            @Argument("blockEntityType") byte blockEntityType,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.BLOCK_ENTITY_TYPE, value = "mob_spawner")) int mobSpawnerId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.BLOCK_ENTITY_TYPE, value = "command_block")) int commandBlockId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.BLOCK_ENTITY_TYPE, value = "beacon")) int beaconId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.BLOCK_ENTITY_TYPE, value = "skull")) int skullId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.BLOCK_ENTITY_TYPE, value = "conduit")) int conduitId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.BLOCK_ENTITY_TYPE, value = "banner")) int bannerId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.BLOCK_ENTITY_TYPE, value = "structure_block")) int structureBlockId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.BLOCK_ENTITY_TYPE, value = "end_gateway")) int endGatewayId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.BLOCK_ENTITY_TYPE, value = "sign")) int signId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.BLOCK_ENTITY_TYPE, value = "bed")) int bedId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.BLOCK_ENTITY_TYPE, value = "jigsaw")) int jigsawId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.BLOCK_ENTITY_TYPE, value = "campfire")) int campfireId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.BLOCK_ENTITY_TYPE, value = "beehive")) int beehiveId
    ) {
        return switch (blockEntityType) {
            case 1 -> mobSpawnerId;
            case 2 -> commandBlockId;
            case 3 -> beaconId;
            case 4 -> skullId;
            case 5 -> conduitId;
            case 6 -> bannerId;
            case 7 -> structureBlockId;
            case 8 -> endGatewayId;
            case 9 -> signId;
            case 11 -> bedId;
            case 12 -> jigsawId;
            case 13 -> campfireId;
            case 14 -> beehiveId;
            default -> mobSpawnerId;
        };
    }

    public static void preprocessBlockEntityData(
            NbtCompound data,
            @Argument("blockEntityType") int blockEntityType
    ) {
        if (data == null) {
            return;
        }
        Identifier name = PacketSystem.serverRawIdToId(net.minecraft.util.registry.Registry.BLOCK_ENTITY_TYPE, blockEntityType);
        if (name != null) {
            data.putString("id", name.toString());
        }
    }
}
