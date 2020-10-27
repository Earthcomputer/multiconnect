package net.earthcomputer.multiconnect.protocols.v1_9_2;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.ChunkData;
import net.earthcomputer.multiconnect.protocols.generic.ChunkDataTranslator;
import net.earthcomputer.multiconnect.protocols.generic.PacketInfo;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_9_4.Protocol_1_9_4;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;

public class Protocol_1_9_2 extends Protocol_1_9_4 {

    private static final Logger LOGGER = LogManager.getLogger("multiconnect");

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
            buf.readBytesSingleAlloc(dataSize); // data
            buf.disablePassthroughMode();
            buf.pendingRead(VarInt.class, new VarInt(0)); // block entity count
            buf.applyPendingReads();
        });
    }

    @Override
    public void postTranslateChunk(ChunkDataTranslator translator, ChunkData data) {
        // add block entities to blocks that have them
        int minX = translator.getPacket().getX() * 16;
        int minZ = translator.getPacket().getZ() * 16;
        for (int sectionY = 0; sectionY < 16; sectionY++) {
            if (data.getSections()[sectionY] != null) {
                for (BlockPos pos : BlockPos.iterate(minX, 16 * sectionY, minZ, minX + 15, 16 * sectionY + 15, minZ + 15)) {
                    BlockState state = data.getBlockState(pos);
                    if (state.getBlock().hasBlockEntity()) {
                        BlockEntityType<?> blockEntityType = null;
                        for (BlockEntityType<?> type : Registry.BLOCK_ENTITY_TYPE) {
                            if (type.supports(state.getBlock())) {
                                blockEntityType = type;
                                break;
                            }
                        }
                        if (blockEntityType != null) {
                            CompoundTag nbt = new CompoundTag();
                            String blockEntityId;
                            if (blockEntityType == BlockEntityType.BED) {
                                blockEntityId = "minecraft:bed"; // block entity that was added in 1.12
                            } else {
                                blockEntityId = Protocol_1_10.getBlockEntityId(blockEntityType);
                                if (blockEntityId == null) {
                                    LOGGER.warn("Block entity " + Registry.BLOCK_ENTITY_TYPE.getId(blockEntityType) + " has no 1.10 ID but tried to be created in chunk data");
                                    continue;
                                }
                            }
                            nbt.putString("id", blockEntityId);
                            nbt.putInt("x", pos.getX());
                            nbt.putInt("y", pos.getY());
                            nbt.putInt("z", pos.getZ());
                            translator.getPacket().getBlockEntityTagList().add(nbt);
                        }
                    }
                }
            }
        }

        super.postTranslateChunk(translator, data);
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
