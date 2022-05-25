package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.*;
import net.earthcomputer.multiconnect.api.Protocols;

import java.util.List;
import java.util.Optional;

@MessageVariant
public class SPacketSetTradeOffers {
    public int syncId;
    @Length(type = Types.UNSIGNED_BYTE)
    public List<Trade_Latest> trades;
    public int villagerLevel;
    public int experience;
    public boolean isRegularVillager;
    public boolean canRestock;

    @MessageVariant(minVersion = Protocols.V1_14_3)
    public static class Trade_Latest implements Trade{
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
    }
    @Message
    public interface Trade {

    }
}
