package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.minecraft.util.math.BlockPos;

@MessageVariant(maxVersion = Protocols.V1_13_2)
public class BlockPos_1_13_2 implements CommonTypes.BlockPos {
    @Type(Types.LONG)
    @Introduce(direction = Introduce.Direction.FROM_NEWER, compute = "computePackedData")
    public long packedData;

    public static long computePackedData(@Argument("packedData") long packedData) {
        int x = (int) (packedData >> 38);
        int y = (int) (packedData << 52 >> 52);
        int z = (int) (packedData << 26 >> 38);
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(y & 0xFFF) << 26) | (long)(z & 0x3FFFFFF);
    }

    @Override
    public BlockPos toMinecraft() {
        int x = (int) (packedData >> 38);
        int y = (int) (packedData << 26 >> 52);
        int z = (int) (packedData << 38 >> 38);
        return new BlockPos(x, y, z);
    }

    public static BlockPos_1_13_2 fromMinecraft(BlockPos blockPos) {
        BlockPos_1_13_2 result = new BlockPos_1_13_2();
        result.packedData = ((long)(blockPos.getX() & 0x3FFFFFF) << 38) | ((long)(blockPos.getY() & 0xFFF) << 26) | (long)(blockPos.getZ() & 0x3FFFFFF);
        return result;
    }
}
