package net.earthcomputer.multiconnect.transformer;

import net.minecraft.network.PacketByteBuf;

public final class ChunkData {

    public static int skipPalette(PacketByteBuf buf) {
        int paletteSize = buf.readByte();
        if (paletteSize <= 8) {
            // array and bimap palette data look the same enough to use the same code here
            int size = buf.readVarInt();
            for (int i = 0; i < size; i++)
                buf.readVarInt(); // state id
        }
        return paletteSize;
    }

}
