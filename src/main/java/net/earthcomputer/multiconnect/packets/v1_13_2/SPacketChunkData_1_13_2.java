package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Datafix;
import net.earthcomputer.multiconnect.ap.DatafixTypes;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.ReturnType;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.ChunkData;
import net.earthcomputer.multiconnect.packets.SPacketChunkData;
import net.earthcomputer.multiconnect.packets.SPacketChunkRenderDistanceCenter;
import net.earthcomputer.multiconnect.packets.v1_14_4.ChunkData_1_14_4;
import net.earthcomputer.multiconnect.packets.v1_14_4.SPacketChunkData_1_14_4;
import net.earthcomputer.multiconnect.protocols.generic.TypedMap;
import net.earthcomputer.multiconnect.protocols.v1_13.ChunkMapManager_1_13_2;
import net.earthcomputer.multiconnect.protocols.v1_13.Protocol_1_13_2;
import net.minecraft.nbt.CompoundTag;
import java.util.ArrayList;
import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_13_2)
public class SPacketChunkData_1_13_2 implements SPacketChunkData {
    @Type(Types.INT)
    public int x;
    @Type(Types.INT)
    public int z;
    public boolean fullChunk;
    public int verticalStripBitmask;
    @Length(raw = true)
    public ChunkData data;
    @Datafix(DatafixTypes.BLOCK_ENTITY)
    public List<CompoundTag> blockEntities;

    @ReturnType(SPacketChunkData.class)
    @ReturnType(SPacketChunkRenderDistanceCenter.class)
    @Handler
    public static List<Object> handle(
            @Argument(value = "this", translate = true) SPacketChunkData_1_14_4 translatedThis,
            @Argument("data") ChunkData data_,
            @FilledArgument TypedMap userData,
            @GlobalData ChunkMapManager_1_13_2 chunkMapManager
    ) {
        List<Object> packets = new ArrayList<>(1);

        if (!chunkMapManager.receivedPosLook()) {
            int minX = chunkMapManager.minChunkX().accumulateAndGet(translatedThis.x, Math::min);
            int minZ = chunkMapManager.minChunkZ().accumulateAndGet(translatedThis.z, Math::min);
            int maxX = chunkMapManager.maxChunkX().accumulateAndGet(translatedThis.x, Math::max);
            int maxZ = chunkMapManager.maxChunkZ().accumulateAndGet(translatedThis.z, Math::max);
            int midX = (maxX + minX) / 2;
            int midZ = (maxZ + minZ) / 2;
            int oldMidX = chunkMapManager.currentMidX().getAndSet(midX);
            int oldMidZ = chunkMapManager.currentMidZ().getAndSet(midZ);
            if (oldMidX != midX || oldMidZ != midZ) {
                var packet = new SPacketChunkRenderDistanceCenter();
                packet.x = midX;
                packet.z = midZ;
                packets.add(packet);
            }
        }

        byte[][] blockLight = new byte[16][];
        byte[][] skyLight = null;

        var data = (ChunkData_1_14_4) data_;
        for (int sectionY = 0, i = 0; sectionY < 16; sectionY++) {
            if ((translatedThis.verticalStripBitmask & (1 << sectionY)) != 0) {
                var section = (ChunkSection_1_13_2) data.sections.get(i++);
                blockLight[sectionY] = section.blockLight;
                if (section.skyLight != null) {
                    if (skyLight == null) {
                        skyLight = new byte[16][];
                    }
                    skyLight[sectionY] = section.skyLight;
                }
            }
        }

        userData.put(Protocol_1_13_2.BLOCK_LIGHT_KEY, blockLight);
        if (skyLight != null) {
            userData.put(Protocol_1_13_2.SKY_LIGHT_KEY, skyLight);
        }

        packets.add(translatedThis);
        return packets;
    }
}
