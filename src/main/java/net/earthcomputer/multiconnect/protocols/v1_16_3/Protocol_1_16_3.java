package net.earthcomputer.multiconnect.protocols.v1_16_3;

import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_16_4.Protocol_1_16_4;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.util.Hand;

import java.util.function.Supplier;

public class Protocol_1_16_3 extends Protocol_1_16_4 {

    public static void registerTranslators() {
        ProtocolRegistry.registerOutboundTranslator(BookUpdateC2SPacket.class, buf -> {
            buf.passthroughWrite(ItemStack.class); // book
            buf.passthroughWrite(Boolean.class); // signed
            Supplier<VarInt> slot = buf.skipWrite(VarInt.class);
            buf.pendingWrite(Hand.class, () -> slot.get().get() == 40 ? Hand.OFF_HAND : Hand.MAIN_HAND, buf::writeEnumConstant);
        });
    }

}
