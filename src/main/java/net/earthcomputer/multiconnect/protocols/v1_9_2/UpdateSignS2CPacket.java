package net.earthcomputer.multiconnect.protocols.v1_9_2;

import net.minecraft.nbt.CompoundTag;
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
        CompoundTag signTag = new CompoundTag();
        for (int i = 0; i < 4; i++) {
            signTag.putString("Text" + (i + 1), Text.Serializer.toJson(lines[i]));
        }
        listener.onBlockEntityUpdate(new BlockEntityUpdateS2CPacket(pos, 9, signTag));
    }
}
