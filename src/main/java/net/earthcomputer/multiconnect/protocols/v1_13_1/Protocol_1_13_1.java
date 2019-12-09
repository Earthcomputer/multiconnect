package net.earthcomputer.multiconnect.protocols.v1_13_1;

import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Protocol_1_13_1 extends Protocol_1_13_2 {
    @Override
    public void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(ItemStack.class, buf -> {
            int itemId = buf.readShort();
            buf.pendingRead(Boolean.class, itemId != -1);
            if (itemId != -1) {
                buf.pendingRead(VarInt.class, new VarInt(itemId));
            }
        });
        ProtocolRegistry.registerOutboundTranslator(ItemStack.class, buf -> {
            Supplier<Boolean> present = buf.skipWrite(Boolean.class);
            buf.whenWrite(() -> {
                if (present.get()) {
                    Supplier<VarInt> itemId = buf.skipWrite(VarInt.class);
                    buf.pendingWrite(Short.class, () -> (short) itemId.get().get().intValue(), (Consumer<Short>) buf::writeShort);
                } else {
                    buf.pendingWrite(Short.class, () -> (short) -1, (Consumer<Short>) buf::writeShort);
                }
            });
        });
    }
}
