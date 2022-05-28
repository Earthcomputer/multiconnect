package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.ReturnType;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketDifficulty;
import net.earthcomputer.multiconnect.packets.SPacketPlayerRespawn;
import net.earthcomputer.multiconnect.packets.latest.SPacketDifficulty_Latest;
import net.earthcomputer.multiconnect.packets.v1_14_4.SPacketPlayerRespawn_1_14_4;
import net.earthcomputer.multiconnect.protocols.v1_13_2.ChunkMapManager_1_13_2;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MessageVariant(maxVersion = Protocols.V1_13_2)
public class SPacketPlayerRespawn_1_13_2 implements SPacketPlayerRespawn {
    @Type(Types.INT)
    public int dimensionId;
    @Type(Types.UNSIGNED_BYTE)
    public int difficulty;
    @Type(Types.UNSIGNED_BYTE)
    public int gamemode;
    public String genType;

    @ReturnType(SPacketPlayerRespawn.class)
    @ReturnType(SPacketDifficulty.class)
    @Handler
    public static List<Object> splitDifficulty(
            @Argument(value = "this", translate = true) SPacketPlayerRespawn_1_14_4 translatedThis,
            @Argument("difficulty") int difficulty,
            @GlobalData Consumer<ChunkMapManager_1_13_2> chunkMapManagerSetter
    ) {
        List<Object> packets = new ArrayList<>(2);
        packets.add(translatedThis);

        var packet = new SPacketDifficulty_Latest();
        packet.difficulty = difficulty;
        packets.add(packet);

        chunkMapManagerSetter.accept(new ChunkMapManager_1_13_2(false));

        return packets;
    }
}
