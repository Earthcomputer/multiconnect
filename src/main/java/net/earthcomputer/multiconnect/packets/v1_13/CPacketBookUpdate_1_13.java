package net.earthcomputer.multiconnect.packets.v1_13;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketBookUpdate;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.v1_12_2.CPacketCustomPayload_1_12_2;
import net.earthcomputer.multiconnect.packets.v1_12_2.CPacketCustomPayload_1_12_2.BookEdit;
import net.earthcomputer.multiconnect.packets.v1_12_2.CPacketCustomPayload_1_12_2.BookSign;
import net.earthcomputer.multiconnect.packets.v1_12_2.ItemStack_1_12_2;
import net.earthcomputer.multiconnect.packets.v1_13_1.ItemStack_1_13_1;

import java.util.function.Function;

@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13)
public class CPacketBookUpdate_1_13 implements CPacketBookUpdate {
    public CommonTypes.ItemStack stack;
    public boolean sign;

    @Handler(protocol = Protocols.V1_12_2)
    public static CPacketCustomPayload_1_12_2 toCustomPayload(
            @Argument("stack") CommonTypes.ItemStack stack,
            @Argument("sign") boolean sign,
            @FilledArgument(fromVersion = Protocols.V1_13, toVersion = Protocols.V1_12_2) Function<ItemStack_1_13_1, ItemStack_1_12_2> itemStackTranslator
    ) {
        if (sign) {
            var packet = new CPacketCustomPayload_1_12_2.BookSign();
            packet.channel = "MC|BSign";
            packet.stack = itemStackTranslator.apply((ItemStack_1_13_1) stack);
            return packet;
        } else {
            var packet = new CPacketCustomPayload_1_12_2.BookEdit();
            packet.channel = "MC|BEdit";
            packet.stack = itemStackTranslator.apply((ItemStack_1_13_1) stack);
            return packet;
        }
    }
}
