package net.earthcomputer.multiconnect.packets.v1_14_4;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Datafix;
import net.earthcomputer.multiconnect.ap.DatafixTypes;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.packets.ChunkData;
import net.earthcomputer.multiconnect.packets.SPacketChunkData;
import net.earthcomputer.multiconnect.packets.v1_13_2.ChunkSection_1_13_2;
import net.earthcomputer.multiconnect.packets.v1_17_1.ChunkData_1_17_1;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.Heightmap;

import java.util.List;

@MessageVariant(minVersion = Protocols.V1_14, maxVersion = Protocols.V1_14_4)
public class SPacketChunkData_1_14_4 implements SPacketChunkData {
    @Type(Types.INT)
    public int x;
    @Type(Types.INT)
    public int z;
    public boolean fullChunk;
    public int verticalStripBitmask;
    @Introduce(compute = "computeHeightmaps")
    public NbtCompound heightmaps;
    @Length(raw = true)
    public ChunkData data;
    @Datafix(DatafixTypes.BLOCK_ENTITY)
    public List<NbtCompound> blockEntities;

    public static NbtCompound computeHeightmaps(
            @Argument("verticalStripBitmask") int verticalStripBitmask,
            @Argument("data") ChunkData data_,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.BLOCK_STATE, value = "air")) int airId
    ) {
        var data = (ChunkData_1_14_4) data_;
        ChunkSection_1_13_2[] sections = new ChunkSection_1_13_2[16];
        for (int i = 0, j = 0; i < 16; i++) {
            if ((verticalStripBitmask & (1 << i)) != 0) {
                sections[i] = (ChunkSection_1_13_2) data.sections.get(j++);
            }
        }

        var worldSurfacePredicate = Heightmap.Type.WORLD_SURFACE.getBlockPredicate();
        var motionBlockingPredicate = Heightmap.Type.MOTION_BLOCKING.getBlockPredicate();
        long[] worldSurface = new long[36];
        long[] motionBlocking = new long[36];

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int sectionY;
                for (sectionY = 15; sectionY >= 0; sectionY--) {
                    if (sections[sectionY] != null) {
                        break;
                    }
                }
                int y = (sectionY + 1) * 16 - 1;

                boolean findingWorldSurface = true;
                boolean findingMotionBlocking = true;
                for (; y >= 0; y--) {
                    // get the chunk section
                    ChunkSection_1_13_2 section = sections[y >> 4];
                    var blockStates = section == null ? null : (ChunkData_1_17_1.BlockStatePalettedContainer) section.blockStates;

                    // get the state at this position
                    int index = ((y & 15) << 8) | (z << 4) | x;
                    int stateId;
                    if (blockStates == null) {
                        stateId = airId;
                    } else {
                        if (blockStates instanceof ChunkData_1_17_1.BlockStatePalettedContainer.Multiple multiple) {
                            stateId = Utils.getOldPackedBitArrayElement(multiple.data, index, blockStates.paletteSize);
                            stateId = multiple.palette[stateId];
                        } else {
                            var registryPalette = (ChunkData_1_17_1.BlockStatePalettedContainer.RegistryContainer) blockStates;
                            stateId = Utils.getOldPackedBitArrayElement(registryPalette.data, index, blockStates.paletteSize);
                        }
                    }
                    stateId = PacketSystem.serverBlockStateIdToClient(stateId);
                    BlockState state = Block.getStateFromRawId(stateId);

                    // test heightmaps
                    if (findingWorldSurface && worldSurfacePredicate.test(state)) {
                        Utils.setOldPackedBitArrayElement(worldSurface, x + z * 16, y + 1, 9);
                        findingWorldSurface = false;
                    }
                    if (findingMotionBlocking && motionBlockingPredicate.test(state)) {
                        Utils.setOldPackedBitArrayElement(motionBlocking, x + z * 16, y + 1, 9);
                        findingMotionBlocking = false;
                    }

                    if (!findingWorldSurface && !findingMotionBlocking) {
                        break;
                    }
                }
            }
        }

        NbtCompound heightmaps = new NbtCompound();
        heightmaps.putLongArray("WORLD_SURFACE", worldSurface);
        heightmaps.putLongArray("MOTION_BLOCKING", motionBlocking);
        return heightmaps;
    }
}
