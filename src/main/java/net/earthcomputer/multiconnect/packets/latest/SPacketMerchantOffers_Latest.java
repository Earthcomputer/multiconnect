package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketMerchantOffers;

import java.util.List;
import java.util.Optional;

@MessageVariant(minVersion = Protocols.V1_14_3)
public class SPacketMerchantOffers_Latest implements SPacketMerchantOffers {
    public int syncId;
    @Length(type = Types.UNSIGNED_BYTE)
    public List<Trade> trades;
    public int villagerLevel;
    public int experience;
    public boolean isRegularVillager;
    @Introduce(booleanValue = true)
    public boolean canRestock;

    @MessageVariant(minVersion = Protocols.V1_19)
    public static class Trade_Latest implements Trade {
        public CommonTypes.ItemStack input1;
        public CommonTypes.ItemStack output;
        @Introduce(compute = "computeInput2")
        public CommonTypes.ItemStack input2;
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

        public static CommonTypes.ItemStack computeInput2(
                @Argument("input2") Optional<CommonTypes.ItemStack> input2
        ) {
            return input2.orElseGet(ItemStack_Latest.Empty::new);
        }
    }
}
