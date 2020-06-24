package net.earthcomputer.multiconnect.mixin.bridge;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.earthcomputer.multiconnect.protocols.generic.IPacketHandler;
import net.earthcomputer.multiconnect.protocols.generic.PacketInfo;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mixin(targets = "net.minecraft.network.NetworkState$PacketHandler")
public abstract class MixinPacketHandler<T extends PacketListener> implements IPacketHandler<T> {

    @Shadow @Final private List<Supplier<? extends Packet<T>>> packetFactories;

    @Shadow @Final private Object2IntMap<Class<? extends Packet<T>>> packetIds;

    @Unique private Int2ObjectMap<Class<? extends Packet<T>>> packetClassesById = new Int2ObjectOpenHashMap<>();

    @Override
    public void multiconnect_clear() {
        packetFactories.clear();
        packetIds.clear();
        packetClassesById.clear();
    }

    @Override
    public <P extends Packet<T>> IPacketHandler<T> multiconnect_register(Class<P> clazz, Supplier<P> factory) {
        int packetId = this.packetFactories.size();
        int oldPacketId = this.packetIds.put(clazz, packetId);
        if (oldPacketId != -1) {
            String errorMessage = "Packet " + clazz + " is already registered to ID " + oldPacketId;
            LogManager.getLogger().fatal(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        } else {
            this.packetFactories.add(factory);
            return this;
        }
    }

    @Override
    public Class<? extends Packet<T>> multiconnect_getPacketClassById(int id) {
        return packetClassesById.computeIfAbsent(id, k -> packetIds.object2IntEntrySet().stream()
                .filter(it -> it.getIntValue() == id)
                .map(Map.Entry::getKey)
                .findAny().orElse(null));
    }

    @Override
    public List<PacketInfo<? extends Packet<T>>> multiconnect_values() {
        return packetIds.object2IntEntrySet().stream()
                .sorted(Comparator.comparingInt(Object2IntMap.Entry::getIntValue))
                .map(entry -> MixinPacketHandler.<T, Packet<T>>createPacketInfo(entry.getKey(), packetFactories.get(entry.getIntValue())))
                .collect(Collectors.toList());
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static <T extends PacketListener, P extends Packet<T>> PacketInfo<P> createPacketInfo(Class<?> packetClass, Supplier<?> factory) {
        return PacketInfo.of((Class<P>) packetClass, (Supplier<P>) factory);
    }
}
