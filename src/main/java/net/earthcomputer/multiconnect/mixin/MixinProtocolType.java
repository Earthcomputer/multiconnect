package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.IProtocolType;
import net.earthcomputer.multiconnect.impl.IPacketHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.ProtocolType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ProtocolType.class)
public abstract class MixinProtocolType implements IProtocolType {

    @Accessor
    @Override
    public abstract Map<PacketDirection, ? extends IPacketHandler<?>> getField_229711_h_();

    @Shadow @Final private static Map<Class<? extends IPacket<?>>, ProtocolType> STATES_BY_CLASS;

    @Override
    public void multiconnect_onAddPacket(Class<? extends IPacket<?>> packet) {
        STATES_BY_CLASS.put(packet, (ProtocolType) (Object) this);
    }
}
