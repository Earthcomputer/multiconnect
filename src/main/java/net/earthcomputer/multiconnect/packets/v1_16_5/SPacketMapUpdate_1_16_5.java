package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketMapUpdate;
import net.earthcomputer.multiconnect.packets.latest.SPacketMapUpdate_Latest;
import net.earthcomputer.multiconnect.protocols.generic.Key;
import net.earthcomputer.multiconnect.protocols.generic.TypedMap;
import net.earthcomputer.multiconnect.protocols.v1_16_5.mixin.MapStateAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;

import java.util.List;
import java.util.Optional;

@MessageVariant(minVersion = Protocols.V1_14, maxVersion = Protocols.V1_16_5)
public class SPacketMapUpdate_1_16_5 implements SPacketMapUpdate {
    public int mapId;
    public byte scale;
    public boolean showIcons;
    @Introduce(booleanValue = false)
    public boolean locked;
    public List<SPacketMapUpdate.Icon> icons;
    @Type(Types.UNSIGNED_BYTE)
    public int columns;
    @OnlyIf("hasColumns")
    public byte rows;
    @OnlyIf("hasColumns")
    public byte x;
    @OnlyIf("hasColumns")
    public byte z;
    @OnlyIf("hasColumns")
    public byte[] data;

    public static boolean hasColumns(@Argument("columns") int columns) {
        return columns > 0;
    }

    public static final Key<Runnable> POST_HANDLE_MAP_PACKET = Key.create("postHandleMapPacket");

    @Handler
    public static SPacketMapUpdate_Latest handle(
            @Argument("mapId") int mapId,
            @Argument("scale") byte scale,
            @Argument("showIcons") boolean showIcons,
            @Argument("locked") boolean locked,
            @Argument("icons") List<SPacketMapUpdate.Icon> icons,
            @Argument("columns") int columns,
            @Argument("rows") byte rows,
            @Argument("x") byte x,
            @Argument("z") byte z,
            @Argument("data") byte[] data,
            @DefaultConstruct SPacketMapUpdate_Latest newPacket,
            @FilledArgument TypedMap userData
    ) {
        newPacket.mapId = mapId;
        newPacket.scale = scale;
        newPacket.locked = locked;
        newPacket.icons = Optional.of(icons);
        newPacket.columns = columns;
        newPacket.rows = rows;
        newPacket.x = x;
        newPacket.z = z;
        newPacket.data = data;
        userData.put(POST_HANDLE_MAP_PACKET, () -> {
            ClientWorld world = MinecraftClient.getInstance().world;
            if (world != null) {
                String mapName = FilledMapItem.getMapName(mapId);
                MapState mapState = world.getMapState(mapName);
                if (mapState != null) {
                    MapStateAccessor accessor = (MapStateAccessor) mapState;
                    if (showIcons != accessor.isShowIcons()) {
                        accessor.setShowIcons(showIcons);
                        MinecraftClient.getInstance().gameRenderer.getMapRenderer().updateTexture(mapId, mapState);
                    }
                }
            }
        });
        return newPacket;
    }
}
