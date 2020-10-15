package net.earthcomputer.multiconnect.protocols.v1_9_2;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.PacketInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_9_4.Protocol_1_9_4;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;

import java.util.List;
import java.util.Set;

public class Protocol_1_9_2 extends Protocol_1_9_4 {

    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(ChunkDataS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt(); // x
            buf.readInt(); // z
            buf.readBoolean(); // full chunk
            buf.readVarInt(); // vertical strip bitmask
            int dataSize = buf.readVarInt();
            if (dataSize > 2097152) {
                throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
            }
            buf.readBytes(new byte[dataSize]); // data
            buf.disablePassthroughMode();
            buf.pendingRead(VarInt.class, new VarInt(0)); // block entity count
            buf.applyPendingReads();
        });
    }

    @Override
    public List<PacketInfo<?>> getClientboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        insertAfter(packets, TitleS2CPacket.class, PacketInfo.of(UpdateSignS2CPacket.class, UpdateSignS2CPacket::new));
        return packets;
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSource> dispatcher, Set<String> serverCommands) {
        super.registerCommands(dispatcher, serverCommands);
        BrigadierRemover.of(dispatcher).get("stopsound").remove();
    }
}
