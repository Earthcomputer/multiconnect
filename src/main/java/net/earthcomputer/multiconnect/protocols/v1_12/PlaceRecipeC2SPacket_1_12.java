package net.earthcomputer.multiconnect.protocols.v1_12;

import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;

import java.util.List;

public class PlaceRecipeC2SPacket_1_12 implements Packet<ServerPlayPacketListener> {

    private final int syncId;
    private final short transactionId;
    private final List<Transaction> transactionsFromMatrix;
    private final List<Transaction> transactionsToMatrix;

    public PlaceRecipeC2SPacket_1_12(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    public PlaceRecipeC2SPacket_1_12(int syncId, short transactionId, List<Transaction> transactionsFromMatrix, List<Transaction> transactionsToMatrix) {
        this.syncId = syncId;
        this.transactionId = transactionId;
        this.transactionsFromMatrix = transactionsFromMatrix;
        this.transactionsToMatrix = transactionsToMatrix;
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

    public List<Transaction> getTransactionsFromMatrix() {
        return transactionsFromMatrix;
    }

    public List<Transaction> getTransactionsToMatrix() {
        return transactionsToMatrix;
    }

    public static class Transaction {
        public ItemStack originalStack;
        public ItemStack stack;
        public ItemStack placedOn;
        public int craftingSlot;
        public int invSlot;

        public Transaction(ItemStack originalStack, ItemStack stack, ItemStack placedOn, int craftingSlot, int invSlot) {
            this.originalStack = originalStack;
            this.stack = stack.copy();
            this.placedOn = placedOn;
            this.craftingSlot = craftingSlot;
            this.invSlot = invSlot;
        }

        @Override
        public String toString() {
            return stack + " / " + originalStack + ": " + craftingSlot + " <-> " + invSlot;
        }
    }

}
