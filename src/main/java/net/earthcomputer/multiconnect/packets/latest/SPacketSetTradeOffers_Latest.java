package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketSetTradeOffers;

import java.util.List;
import java.util.Optional;

@MessageVariant(minVersion = Protocols.V1_14_3)
public class SPacketSetTradeOffers_Latest implements SPacketSetTradeOffers {
    public int syncId;
    @Length(type = Types.UNSIGNED_BYTE)
    public List<Trade> trades;
    public int villagerLevel;
    public int experience;
    public boolean isRegularVillager;
    @Introduce(booleanValue = true)
    public boolean canRestock;

    @MessageVariant(minVersion = Protocols.V1_14_4)
    public static class Trade_Latest implements Trade {
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
        @Introduce(intValue = 0)
        public int demand;
    }
}
