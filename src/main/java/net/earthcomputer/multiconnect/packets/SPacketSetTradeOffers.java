package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

import java.util.List;
import java.util.Optional;

@Message
public class SPacketSetTradeOffers {
    public int syncId;
    @Length(type = Types.UNSIGNED_BYTE)
    public List<Trade> trades;
    public int villagerLevel;
    public int experience;
    public boolean isRegularVillager;
    public boolean canRestock;

    @Message
    public static class Trade {
        public CommonTypes.ItemStack input1;
        public CommonTypes.ItemStack output;
        public Optional<CommonTypes.ItemStack> input2;
        public boolean tradeDisabled;
        @Type(Types.INT)
        public int uses;
        @Type(Types.INT)
        public int maxUses;
        @Type(Types.INT)
        public int xp;
        @Type(Types.INT)
        public int specialPrice;
        public float priceMultiplier;
        @Type(Types.INT)
        public int demand;
    }
}
