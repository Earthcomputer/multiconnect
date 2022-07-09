package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.ReturnType;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketChangeDifficulty;
import net.earthcomputer.multiconnect.packets.SPacketSetChunkCacheRadius;
import net.earthcomputer.multiconnect.packets.SPacketLogin;
import net.earthcomputer.multiconnect.packets.latest.SPacketChangeDifficulty_Latest;
import net.earthcomputer.multiconnect.packets.v1_14_4.SPacketLogin_1_14_4;
import net.earthcomputer.multiconnect.protocols.v1_13.ChunkMapManager_1_13_2;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_2)
public class SPacketLogin_1_13_2 implements SPacketLogin {
    @Type(Types.INT)
    public int entityId;
    @Type(Types.UNSIGNED_BYTE)
    public int gamemode;
    @Type(Types.INT)
    public int dimensionId;
    @Type(Types.UNSIGNED_BYTE)
    public int difficulty;
    @Type(Types.UNSIGNED_BYTE)
    public int maxPlayers;
    public String genType;
    public boolean reducedDebugInfo;

    @ReturnType(SPacketLogin.class)
    @ReturnType(SPacketChangeDifficulty.class)
    @ReturnType(SPacketSetChunkCacheRadius.class)
    @Handler
    public static List<Object> handle(
            @Argument(value = "this", translate = true) SPacketLogin_1_14_4 translatedThis,
            @Argument("difficulty") int difficulty,
            @GlobalData Consumer<ChunkMapManager_1_13_2> chunkMapManagerSetter
    ) {
        List<Object> packets = new ArrayList<>(3);
        packets.add(translatedThis);

        {
            var packet = new SPacketChangeDifficulty_Latest();
            packet.difficulty = difficulty;
            packets.add(packet);
        }
        {
            var packet = new SPacketSetChunkCacheRadius();
            packet.viewDistance = 64;
            packets.add(packet);
        }

        chunkMapManagerSetter.accept(new ChunkMapManager_1_13_2(false));

        return packets;
    }
}
