package net.earthcomputer.multiconnect.protocols.v1_8;

import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.util.math.BlockPos;

public class PlayerUseItemC2SPacket_1_8 implements Packet<ServerPlayPacketListener> {
    private static final BlockPos NULL_POS = new BlockPos(-1, -1, -1);
    private BlockPos pos;
    private int hitSide;
    private ItemStack stack;
    private float fractionalX;
    private float fractionalY;
    private float fractionalZ;

    public PlayerUseItemC2SPacket_1_8() {
    }

    public PlayerUseItemC2SPacket_1_8(ItemStack stack) {
        this(NULL_POS, 255, stack, 0, 0, 0);
    }

    public PlayerUseItemC2SPacket_1_8(BlockPos pos, int hitSide, ItemStack stack, float fractionalX, float fractionalY, float fractionalZ) {
        this.pos = pos;
        this.hitSide = hitSide;
        this.stack = stack;
        this.fractionalX = fractionalX;
        this.fractionalY = fractionalY;
        this.fractionalZ = fractionalZ;
    }

    @Override
    public void read(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeByte(hitSide);
        buf.writeItemStack(stack);
        buf.writeByte((int)(fractionalX * 16));
        buf.writeByte((int)(fractionalY * 16));
        buf.writeByte((int)(fractionalZ * 16));
    }

    @Override
    public void apply(ServerPlayPacketListener listener) {
        throw new UnsupportedOperationException();
    }
}
