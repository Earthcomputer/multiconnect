package net.earthcomputer.multiconnect.protocols.v1_12;

import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;

import java.util.List;

public class PlaceRecipeC2SPacket_1_12 implements Packet<ServerPlayPacketListener> {

    private int syncId;
    private short transactionId;
    private List<Transaction> transactionsFromMatrix;
    private List<Transaction> transactionsToMatrix;

    public PlaceRecipeC2SPacket_1_12() {}

    public PlaceRecipeC2SPacket_1_12(int syncId, short transactionId, List<Transaction> transactionsFromMatrix, List<Transaction> transactionsToMatrix) {
        this.syncId = syncId;
        this.transactionId = transactionId;
        this.transactionsFromMatrix = transactionsFromMatrix;
        this.transactionsToMatrix = transactionsToMatrix;
    }

    @Override
    public void read(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeByte(syncId);
        buf.writeShort(transactionId);
        writeTransactions(buf, transactionsFromMatrix);
        writeTransactions(buf, transactionsToMatrix);
    }

    private void writeTransactions(PacketByteBuf buf, List<Transaction> transactions) {
        buf.writeShort(transactions.size());
        for (Transaction transaction : transactions) {
            buf.writeItemStack(transaction.stack);
            buf.writeByte(transaction.craftingSlot);
            buf.writeByte(transaction.invSlot);
        }
    }

    @Override
    public void apply(ServerPlayPacketListener listener) {
        throw new UnsupportedOperationException();
    }

    public static class Transaction {
        public ItemStack stack;
        public int craftingSlot;
        public int invSlot;

        public Transaction(ItemStack stack, int craftingSlot, int invSlot) {
            this.stack = stack.copy();
            this.craftingSlot = craftingSlot;
            this.invSlot = invSlot;
        }
    }

}
