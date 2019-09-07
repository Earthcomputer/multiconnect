package net.earthcomputer.multiconnect.protocols.v1_14_2;

import net.earthcomputer.multiconnect.impl.TransformerByteBuf;
import net.earthcomputer.multiconnect.protocols.v1_14_3.Protocol_1_14_3;
import net.minecraft.client.network.packet.SetTradeOffersPacket;
import net.minecraft.network.Packet;

import java.util.List;

public class Protocol_1_14_2 extends Protocol_1_14_3 {
    @Override
    public void transformPacketClientbound(Class<? extends Packet<?>> packetClass, List<TransformerByteBuf> transformers) {
        super.transformPacketClientbound(packetClass, transformers);

        if (packetClass == SetTradeOffersPacket.class) {
            transformers.add(new TransformerByteBuf() {
                int size;
                int boolCount = 0;

                @Override
                public byte readByte() {
                    byte ret = super.readByte();
                    size = ret & 0xff;
                    return ret;
                }

                @Override
                public boolean readBoolean() {
                    if (isTopLevel()) {
                        //noinspection SimplifiableConditionalExpression
                        return boolCount++ == size * 2 + 1 ? true : super.readBoolean();
                    } else {
                        return super.readBoolean();
                    }
                }
            });
        }
    }
}
