package net.earthcomputer.multiconnect.packets.v1_14_3;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketMerchantOffers;

import java.util.Optional;

@MessageVariant(maxVersion = Protocols.V1_14_3)
public class Trade_1_14_3 implements SPacketMerchantOffers.Trade {
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
