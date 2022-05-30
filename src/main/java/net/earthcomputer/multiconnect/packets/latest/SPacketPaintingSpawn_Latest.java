package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketPaintingSpawn;

import java.util.UUID;

@MessageVariant(minVersion = Protocols.V1_13)
public class SPacketPaintingSpawn_Latest implements SPacketPaintingSpawn {
    public int entityId;
    public UUID uuid;
    @Registry(Registries.MOTIVE)
    @Introduce(compute = "computeMotive")
    public int motive;
    public CommonTypes.BlockPos pos;
    public byte direction;

    public static int computeMotive(
            @Argument("motive") String motive,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "kebab")) int kebabId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "aztec")) int aztecId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "alban")) int albanId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "aztec2")) int aztec2Id,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "bomb")) int bombId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "plant")) int plantId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "wasteland")) int wastelandId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "pool")) int poolId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "courbet")) int courbetId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "sea")) int seaId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "sunset")) int sunsetId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "creebet")) int creebetId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "wanderer")) int wandererId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "graham")) int grahamId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "match")) int matchId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "bust")) int bustId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "stage")) int stageId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "void")) int voidId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "skull_and_roses")) int skullAndRosesId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "wither")) int witherId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "fighters")) int fightersId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "pointer")) int pointerId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "pigscene")) int pigsceneId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "burning_skull")) int burningSkullId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "skeleton")) int skeletonId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.MOTIVE, value = "donkey_kong")) int donkeyKongId
    ) {
        return switch (motive) {
            case "Kebab" -> kebabId;
            case "Aztec" -> aztecId;
            case "Alban" -> albanId;
            case "Aztec2" -> aztec2Id;
            case "Bomb" -> bombId;
            case "Plant" -> plantId;
            case "Wasteland" -> wastelandId;
            case "Pool" -> poolId;
            case "Courbet" -> courbetId;
            case "Sea" -> seaId;
            case "Sunset" -> sunsetId;
            case "Creebet" -> creebetId;
            case "Wanderer" -> wandererId;
            case "Graham" -> grahamId;
            case "Match" -> matchId;
            case "Bust" -> bustId;
            case "Stage" -> stageId;
            case "Void" -> voidId;
            case "SkullAndRoses" -> skullAndRosesId;
            case "Wither" -> witherId;
            case "Fighters" -> fightersId;
            case "Pointer" -> pointerId;
            case "Pigscene" -> pigsceneId;
            case "BurningSkull" -> burningSkullId;
            case "Skeleton" -> skeletonId;
            case "DonkeyKong" -> donkeyKongId;
            default -> kebabId;
        };
    }
}
