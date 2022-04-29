package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.impl.DelayedPacketSender;
import net.earthcomputer.multiconnect.packets.SPacketMapUpdate;
import net.earthcomputer.multiconnect.protocols.v1_16_5.mixin.MapStateAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;

import java.util.List;
import java.util.Optional;

@MessageVariant
public class SPacketMapUpdate_1_16_5 {
    public int mapId;
    public byte scale;
    public boolean showIcons;
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

    @Handler
    public static void handle(
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
            @DefaultConstruct SPacketMapUpdate newPacket,
            @FilledArgument DelayedPacketSender<SPacketMapUpdate> mapPacketSender
    ) {
        MinecraftClient.getInstance().execute(() -> {
            ClientWorld world = MinecraftClient.getInstance().world;
            if (world != null) {
                newPacket.mapId = mapId;
                newPacket.scale = scale;
                newPacket.locked = locked;
                newPacket.icons = Optional.of(icons);
                newPacket.columns = columns;
                newPacket.rows = rows;
                newPacket.x = x;
                newPacket.z = z;
                newPacket.data = data;
                mapPacketSender.send(newPacket);
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
    }
}
