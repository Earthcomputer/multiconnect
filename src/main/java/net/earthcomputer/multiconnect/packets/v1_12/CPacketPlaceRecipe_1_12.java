package net.earthcomputer.multiconnect.packets.v1_12;

import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Sendable;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;

import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_12)
@Sendable(from = Protocols.V1_12)
public class CPacketPlaceRecipe_1_12 {
    @Type(Types.UNSIGNED_BYTE)
    public int syncId;
    public short transactionId;
    @Length(type = Types.SHORT)
    public List<Transaction> transactionsFromMatrix;
    @Length(type = Types.SHORT)
    public List<Transaction> transactionsToMatrix;

    @MessageVariant
    public static class Transaction {
        public CommonTypes.ItemStack stack;
        public byte craftingSlot;
        public byte invSlot;
    }
}
