package net.earthcomputer.multiconnect.packets.v1_14_2;

import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketMerchantOffers;

import java.util.List;

@MessageVariant(minVersion = Protocols.V1_14, maxVersion = Protocols.V1_14_2)
public class SPacketMerchantOffers_1_14_2 implements SPacketMerchantOffers {
    public int syncId;
    @Length(type = Types.UNSIGNED_BYTE)
    public List<Trade> trades;
    public int villagerLevel;
    public int experience;
    public boolean isRegularVillager;
}
