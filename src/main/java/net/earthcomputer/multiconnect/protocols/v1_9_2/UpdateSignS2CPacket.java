package net.earthcomputer.multiconnect.protocols.v1_9_2;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.Utils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class UpdateSignS2CPacket implements Packet<ClientPlayPacketListener> {
    private final BlockPos pos;
    private final Text[] lines = new Text[4];

    public UpdateSignS2CPacket(PacketByteBuf buf) {
        pos = buf.readBlockPos();
        for (int i = 0; i < 4; i++) {
            lines[i] = buf.readText();
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        NbtCompound signNbt = new NbtCompound();
        for (int i = 0; i < 4; i++) {
            signNbt.putString("Text" + (i + 1), Text.Serializer.toJson(lines[i]));
        }
        listener.onBlockEntityUpdate(Utils.createPacket(BlockEntityUpdateS2CPacket.class, BlockEntityUpdateS2CPacket::new, Protocols.V1_9_2, buf -> {
            buf.pendingRead(BlockPos.class, pos);
            buf.pendingRead(Byte.class, (byte) 9); // sign type
            buf.pendingRead(NbtCompound.class, signNbt);
            buf.applyPendingReads();
        }));
    }
}
