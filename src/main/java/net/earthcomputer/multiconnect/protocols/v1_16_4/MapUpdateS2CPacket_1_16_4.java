package net.earthcomputer.multiconnect.protocols.v1_16_4;

import net.earthcomputer.multiconnect.protocols.v1_16_4.mixin.MapStateAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;

import java.util.Arrays;

public class MapUpdateS2CPacket_1_16_4 implements Packet<ClientPlayPacketListener> {
    private final int id;
    private final byte scale;
    private final boolean showIcons;
    private final boolean locked;
    private final MapIcon[] icons;
    private final MapState.UpdateData data;

    public MapUpdateS2CPacket_1_16_4(PacketByteBuf buf) {
        id = buf.readVarInt();
        scale = buf.readByte();
        showIcons = buf.readBoolean();
        locked = buf.readBoolean();
        icons = new MapIcon[buf.readVarInt()];
        for (int i = 0; i < icons.length; i++) {
            MapIcon.Type type = buf.readEnumConstant(MapIcon.Type.class);
            icons[i] = new MapIcon(type, buf.readByte(), buf.readByte(), (byte)(buf.readByte() & 15), buf.readBoolean() ? buf.readText() : null);
        }

        int width = buf.readUnsignedByte();
        if (width > 0) {
            int height = buf.readUnsignedByte();
            int startX = buf.readUnsignedByte();
            int startY = buf.readUnsignedByte();
            byte[] mapColors = buf.readByteArray();
            data = new MapState.UpdateData(startX, startY, width, height, mapColors);
        } else {
            data = null;
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        NetworkThreadUtils.forceMainThread(this, listener, MinecraftClient.getInstance());

        MapUpdateS2CPacket packet = new MapUpdateS2CPacket(id, scale, locked, Arrays.asList(icons), data);
        listener.onMapUpdate(packet);

        assert MinecraftClient.getInstance().world != null;
        String mapName = FilledMapItem.getMapName(id);
        MapState mapState = MinecraftClient.getInstance().world.getMapState(mapName);
        if (mapState != null) {
            MapStateAccessor accessor = (MapStateAccessor) mapState;
            if (showIcons != accessor.isShowIcons()) {
                accessor.setShowIcons(showIcons);
                MinecraftClient.getInstance().gameRenderer.getMapRenderer().updateTexture(id, mapState);
            }
        }
    }
}
