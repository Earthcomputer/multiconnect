package net.earthcomputer.multiconnect.packets.v1_16_3;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketBookUpdate;
import net.earthcomputer.multiconnect.packets.CommonTypes;

@MessageVariant(maxVersion = Protocols.V1_16_3)
public class CPacketBookUpdate_1_16_3 implements CPacketBookUpdate {
    public CommonTypes.ItemStack stack;
    public boolean sign;
    @Introduce(compute = "computeHand")
    public CommonTypes.Hand hand;

    public static CommonTypes.Hand computeHand(@Argument("slot") int slot) {
        return slot == 40 ? CommonTypes.Hand.OFF_HAND : CommonTypes.Hand.MAIN_HAND;
    }
}
