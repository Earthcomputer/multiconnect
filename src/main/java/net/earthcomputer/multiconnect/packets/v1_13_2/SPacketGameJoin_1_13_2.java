package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.ReturnType;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketChunkLoadDistance;
import net.earthcomputer.multiconnect.packets.SPacketDifficulty;
import net.earthcomputer.multiconnect.packets.SPacketGameJoin;
import net.earthcomputer.multiconnect.packets.latest.SPacketDifficulty_Latest;
import net.earthcomputer.multiconnect.packets.v1_14_4.SPacketGameJoin_1_14_4;
import net.earthcomputer.multiconnect.protocols.v1_13_2.ChunkMapManager_1_13_2;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MessageVariant(maxVersion = Protocols.V1_13_2)
public class SPacketGameJoin_1_13_2 implements SPacketGameJoin {
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

    @ReturnType(SPacketGameJoin.class)
    @ReturnType(SPacketDifficulty.class)
    @ReturnType(SPacketChunkLoadDistance.class)
    @Handler
    public static List<Object> handle(
            @Argument(value = "this", translate = true) SPacketGameJoin_1_14_4 translatedThis,
            @Argument("difficulty") int difficulty,
            @GlobalData Consumer<ChunkMapManager_1_13_2> chunkMapManagerSetter
    ) {
        List<Object> packets = new ArrayList<>(3);
        packets.add(translatedThis);

        {
            var packet = new SPacketDifficulty_Latest();
            packet.difficulty = difficulty;
            packets.add(packet);
        }
        {
            var packet = new SPacketChunkLoadDistance();
            packet.viewDistance = 64;
            packets.add(packet);
        }

        chunkMapManagerSetter.accept(new ChunkMapManager_1_13_2(false));

        return packets;
    }
}
