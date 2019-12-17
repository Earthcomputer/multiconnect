package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.INetworkState;
import net.earthcomputer.multiconnect.impl.IPacketHandler;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(NetworkState.class)
public abstract class MixinNetworkState implements INetworkState {

    @Accessor
    @Override
    public abstract Map<NetworkSide, ? extends IPacketHandler<?>> getPacketHandlers();

    @Shadow @Final private static Map<Class<? extends Packet<?>>, NetworkState> HANDLER_STATE_MAP;

    @Override
    public void multiconnect_onAddPacket(Class<? extends Packet<?>> packet) {
        HANDLER_STATE_MAP.put(packet, (NetworkState) (Object) this);
    }
}
