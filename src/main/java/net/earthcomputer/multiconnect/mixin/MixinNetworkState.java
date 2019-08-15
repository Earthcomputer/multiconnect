package net.earthcomputer.multiconnect.mixin;

import com.google.common.collect.BiMap;
import net.earthcomputer.multiconnect.impl.INetworkState;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(NetworkState.class)
public abstract class MixinNetworkState implements INetworkState {

    @Accessor
    @Override
    public abstract Map<NetworkSide, BiMap<Integer, Class<? extends Packet<?>>>> getPacketHandlerMap();

    @Shadow protected abstract NetworkState addPacket(NetworkSide networkSide_1, Class<? extends Packet<?>> class_1);

    @Override
    public void multiconnect_addPacket(NetworkSide side, Class<? extends Packet<?>> packet) {
        addPacket(side, packet);
    }
}
