package net.earthcomputer.multiconnect.protocols.v1_16_5.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CommandTreeS2CPacket.class)
public interface CommandTreeS2CAccessor {
    @Invoker
    static CommandTreeS2CPacket.CommandNodeData callReadCommandNode(PacketByteBuf packetByteBuf) {
        return MixinHelper.fakeInstance();
    }
}
