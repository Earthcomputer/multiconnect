package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.ReturnType;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketAddPainting;
import net.earthcomputer.multiconnect.packets.SPacketSetEntityData;
import net.earthcomputer.multiconnect.packets.latest.SPacketAddEntity_Latest;
import net.earthcomputer.multiconnect.protocols.v1_18.mixin.PaintingAccessor;
import net.minecraft.core.Direction;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
public class SPacketAddPainting_1_18_2 implements SPacketAddPainting {
    public int entityId;
    public UUID uuid;
    @Registry(Registries.PAINTING_VARIANT)
    @Introduce(compute = "computeMotive")
    public int motive;
    public CommonTypes.BlockPos pos;
    public byte direction;

    @ReturnType(SPacketAddEntity_Latest.class)
    @ReturnType(SPacketSetEntityData.class)
    @Handler
    public static List<Object> handle(
            @Argument("this") SPacketAddPainting_1_18_2 self,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "painting")) int paintingId,
            @DefaultConstruct CommonTypes.EntityTrackerEntry.TrackedData.PaintingVariant trackedData,
            @DefaultConstruct CommonTypes.EntityTrackerEntry.Empty emptyEntry
    ) {
        List<Object> packets = new ArrayList<>(2);

        Direction direction = Direction.from2DDataValue(self.direction);

        {
            var packet = new SPacketAddEntity_Latest();
            packet.entityId = self.entityId;
            packet.uuid = self.uuid;
            packet.type = paintingId;
            packet.data = direction.get3DDataValue();
            var mcPos = self.pos.toMinecraft();
            packet.x = mcPos.getX() + 0.5 - 0.46875 * direction.getStepX();
            packet.y = mcPos.getY() + 0.5;
            packet.z = mcPos.getZ() + 0.5 - 0.46875 * direction.getStepZ();
            packets.add(packet);
        }

        {
            var packet = new SPacketSetEntityData();
            packet.entityId = self.entityId;
            var firstEntry = new CommonTypes.EntityTrackerEntry.Other();
            firstEntry.field = PaintingAccessor.getDataPaintingVariantId().getId();
            trackedData.value = self.motive;
            firstEntry.trackedData = trackedData;
            firstEntry.next = emptyEntry;
            packet.entries = firstEntry;
            packets.add(packet);
        }

        return packets;
    }

    public static int computeMotive(
            @Argument("motive") String motive,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "kebab")) int kebabId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "aztec")) int aztecId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "alban")) int albanId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "aztec2")) int aztec2Id,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "bomb")) int bombId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "plant")) int plantId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "wasteland")) int wastelandId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "pool")) int poolId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "courbet")) int courbetId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "sea")) int seaId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "sunset")) int sunsetId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "creebet")) int creebetId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "wanderer")) int wandererId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "graham")) int grahamId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "match")) int matchId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "bust")) int bustId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "stage")) int stageId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "void")) int voidId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "skull_and_roses")) int skullAndRosesId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "wither")) int witherId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "fighters")) int fightersId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "pointer")) int pointerId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "pigscene")) int pigsceneId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "burning_skull")) int burningSkullId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "skeleton")) int skeletonId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.PAINTING_VARIANT, value = "donkey_kong")) int donkeyKongId
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
