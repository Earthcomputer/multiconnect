package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.ReturnType;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketSetChunkCacheCenter;
import net.earthcomputer.multiconnect.packets.SPacketPlayerPosition;
import net.earthcomputer.multiconnect.packets.v1_16_5.SPacketPlayerPosition_1_16_5;
import net.earthcomputer.multiconnect.protocols.v1_13.ChunkMapManager_1_13_2;
import net.minecraft.util.Mth;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MessageVariant(maxVersion = Protocols.V1_13_2)
public class SPacketPlayerPosition_1_13_2 implements SPacketPlayerPosition {
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public byte flags;
    public int teleportId;

    @ReturnType(SPacketPlayerPosition.class)
    @ReturnType(SPacketSetChunkCacheCenter.class)
    @Handler
    public static List<Object> handle(
            @Argument(value = "this", translate = true) SPacketPlayerPosition_1_16_5 translatedThis,
            @GlobalData Consumer<ChunkMapManager_1_13_2> chunkMapManagerSetter
    ) {
        List<Object> packets = new ArrayList<>(2);
        packets.add(translatedThis);

        var packet = new SPacketSetChunkCacheCenter();
        packet.x = Mth.floor(translatedThis.x / 16);
        packet.z = Mth.floor(translatedThis.z / 16);
        packets.add(packet);

        chunkMapManagerSetter.accept(new ChunkMapManager_1_13_2(true));

        return packets;
    }
}
