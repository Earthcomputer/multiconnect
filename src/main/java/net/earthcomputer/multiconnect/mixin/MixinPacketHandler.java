package net.earthcomputer.multiconnect.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.earthcomputer.multiconnect.impl.IPacketHandler;
import net.earthcomputer.multiconnect.impl.PacketInfo;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
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

@Mixin(targets = "net.minecraft.network.ProtocolType$PacketList")
public abstract class MixinPacketHandler<T extends INetHandler> implements IPacketHandler<T> {

    @Shadow @Final private List<Supplier<? extends IPacket<T>>> field_229716_b_;

    @Shadow @Final private Object2IntMap<Class<? extends IPacket<T>>> field_229715_a_;

    @Unique private Int2ObjectMap<Class<? extends IPacket<T>>> packetClassesById = new Int2ObjectOpenHashMap<>();

    @Override
    public void multiconnect_clear() {
        field_229716_b_.clear();
        field_229715_a_.clear();
        packetClassesById.clear();
    }

    @Override
    public <P extends IPacket<T>> IPacketHandler<T> multiconnect_register(Class<P> clazz, Supplier<P> factory) {
        int packetId = this.field_229716_b_.size();
        int oldPacketId = this.field_229715_a_.put(clazz, packetId);
        if (oldPacketId != -1) {
            String errorMessage = "Packet " + clazz + " is already registered to ID " + oldPacketId;
            LogManager.getLogger().fatal(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        } else {
            this.field_229716_b_.add(factory);
            return this;
        }
    }

    @Override
    public Class<? extends IPacket<T>> multiconnect_getPacketClassById(int id) {
        return packetClassesById.computeIfAbsent(id, k -> field_229715_a_.object2IntEntrySet().stream()
                .filter(it -> it.getIntValue() == id)
                .map(Map.Entry::getKey)
                .findAny().orElse(null));
    }

    @Override
    public List<PacketInfo<? extends IPacket<T>>> multiconnect_values() {
        return field_229715_a_.object2IntEntrySet().stream()
                .sorted(Comparator.comparingInt(Object2IntMap.Entry::getIntValue))
                .map(entry -> MixinPacketHandler.<T, IPacket<T>>createPacketInfo(entry.getKey(), field_229716_b_.get(entry.getIntValue())))
                .collect(Collectors.toList());
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static <T extends INetHandler, P extends IPacket<T>> PacketInfo<P> createPacketInfo(Class<?> packetClass, Supplier<?> factory) {
        return PacketInfo.of((Class<P>) packetClass, (Supplier<P>) factory);
    }
}
