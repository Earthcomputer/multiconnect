package net.earthcomputer.multiconnect.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.mixin.connect.ConnectionAccessor;
import net.earthcomputer.multiconnect.protocols.generic.TypedMap;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PacketSystem {
    private static final Logger LOGGER = LogUtils.getLogger();

    private PacketSystem() {}

    private static final Int2ObjectMap<ProtocolClassProxy> protocolClasses = Util.make(new Int2ObjectOpenHashMap<>(), map -> {
        for (ConnectionMode protocol : ConnectionMode.protocolValues()) {
            String protocolName = protocol.getName();

            // handle snapshot version for snapshot development
            if (!SharedConstants.getCurrentVersion().isStable() && protocolName.equals(SharedConstants.getCurrentVersion().getId())) {
                protocolName = SharedConstants.getCurrentVersion().getReleaseTarget();
            }

            Class<?> clazz;
            try {
                clazz = Class.forName("net.earthcomputer.multiconnect.generated.Protocol_" + protocolName.replace('.', '_'));
            } catch (ClassNotFoundException e) {
                // TODO: make this a hard error once the packet system is complete
                LOGGER.warn("Protocol class not found for {}", protocolName);
                continue;
            }
            map.put(protocol.getValue(), new ProtocolClassProxy(clazz, protocol.getValue()));
        }
    });

    private static final MethodHandle defaultConstructor = Util.make(() -> {
        Class<?> clazz;
        try {
            clazz = Class.forName("net.earthcomputer.multiconnect.generated.DefaultConstructors");
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Could not find DefaultConstructors class", e);
        }
        return findMethodHandle(clazz, "construct", Object.class, Class.class);
    });

    private static final LoadingCache<Packet<?>, TypedMap> packetUserData = CacheBuilder.newBuilder().weakKeys().build(CacheLoader.from(TypedMap::new));
    private static final Map<Class<?>, Object> globalData = new HashMap<>();

    // TODO: enable threaded translation by default if we decide it's needed
    private static final boolean USE_THREADED_TRANSLATION = Boolean.parseBoolean(System.getProperty("multiconnect.useThreadedTranslation", "false"));
    private static ReadWritePacketExecutor clientboundExecutor;
    private static ReadWritePacketExecutor serverboundExecutor;

    public static void connect() {
        if (USE_THREADED_TRANSLATION) {
            clientboundExecutor = new ReadWritePacketExecutor(true);
            serverboundExecutor = new ReadWritePacketExecutor(false);
        }
    }

    public static void disconnect() {
        joinExecutors();
        globalData.clear();
    }

    private static void joinExecutors() {
        if (clientboundExecutor == null && serverboundExecutor == null) {
            return;
        }

        if (clientboundExecutor != null) {
            clientboundExecutor.shutdown();
        }
        if (serverboundExecutor != null) {
            serverboundExecutor.shutdown();
        }

        long start = System.nanoTime();
        List<CompletableFuture<Void>> futures = new ArrayList<>(2);
        if (clientboundExecutor != null) {
            futures.add(clientboundExecutor.awaitTermination(start));
        }
        if (serverboundExecutor != null) {
            futures.add(serverboundExecutor.awaitTermination(start));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    public static TypedMap getUserData(Packet<?> packet) {
        return packetUserData.getUnchecked(packet);
    }

    @SuppressWarnings("unchecked")
    @Contract("_ -> new")
    public static <T> T defaultConstruct(Class<T> type) {
        try {
            return (T) defaultConstructor.invoke(type);
        } catch (Throwable e) {
            throw PacketIntrinsics.sneakyThrow(e);
        }
    }

    public static void sendToServer(ClientPacketListener connection, int protocol, Object packet) {
        sendToServer(connection, protocol, packet, userData -> {});
    }

    public static void sendToServer(ClientPacketListener connection, int protocol, Object packet, Consumer<TypedMap> userDataSetter) {
        Channel channel = ((ConnectionAccessor) connection.getConnection()).getChannel();
        Runnable send = () -> {
            List<ByteBuf> bufs = new ArrayList<>(1);
            TypedMap userData = new TypedMap();
            userDataSetter.accept(userData);
            protocolClasses.get(ConnectionInfo.protocolVersion).sendToServer(packet, protocol, bufs, connection, globalData, userData);
            PacketIntrinsics.sendRawToServer(connection, bufs);
        };
        if (channel.eventLoop().inEventLoop()) {
            send.run();
        } else {
            channel.eventLoop().execute(send);
        }
    }

    public static void sendToClient(ClientPacketListener connection, int protocol, Object packet) {
        sendToClient(connection, protocol, packet, userData -> {});
    }

    public static void sendToClient(ClientPacketListener connection, int protocol, Object packet, Consumer<TypedMap> userDataSetter) {
        Channel channel = ((ConnectionAccessor) connection.getConnection()).getChannel();
        Runnable send = () -> {
            List<ByteBuf> bufs = new ArrayList<>(1);
            TypedMap userData = new TypedMap();
            userDataSetter.accept(userData);
            protocolClasses.get(protocol).sendToClient(packet, bufs, connection, globalData, userData);
            PacketIntrinsics.sendRawToClient(connection, userData, bufs);
        };
        if (channel.eventLoop().inEventLoop()) {
            send.run();
        } else {
            channel.eventLoop().execute(send);
        }
    }

    public static <T> boolean doesServerKnow(Registry<T> registry, ResourceKey<T> key) {
        return doesServerKnow(registry, key, Objects.requireNonNull(registry.get(key)));
    }

    public static <T> boolean doesServerKnow(Registry<T> registry, T value) {
        Optional<ResourceKey<T>> key = registry.getResourceKey(value);
        if (key.isEmpty()) {
            return false;
        }
        return doesServerKnow(registry, key.get(), value);
    }

    private static <T> boolean doesServerKnow(Registry<T> registry, ResourceKey<T> key, T value) {
        int rawId = registry.getId(value);
        ProtocolClassProxy proxy = protocolClasses.get(ConnectionInfo.protocolVersion);
        return proxy.doesServerKnow(registry.key(), rawId) || proxy.doesServerKnowMulticonnect(key);
    }

    public static boolean doesServerKnowBlockState(BlockState state) {
        int rawId = Block.getId(state);
        ProtocolClassProxy proxy = protocolClasses.get(ConnectionInfo.protocolVersion);
        return proxy.doesServerKnowBlockState(rawId) || proxy.doesServerKnowBlockStateMulticonnect(state);
    }

    public static int serverRawIdToClient(Registry<?> registry, int serverRawId) {
        return serverRawIdToClient(ConnectionInfo.protocolVersion, registry, serverRawId);
    }

    public static int serverRawIdToClient(int serverVersion, Registry<?> registry, int serverRawId) {
        return protocolClasses.get(serverVersion).remapSInt(registry.key(), serverRawId);
    }

    public static int clientRawIdToServer(Registry<?> registry, int clientRawId) {
        return clientRawIdToServer(ConnectionInfo.protocolVersion, registry, clientRawId);
    }

    public static int clientRawIdToServer(int serverVersion, Registry<?> registry, int clientRawId) {
        return protocolClasses.get(serverVersion).remapCInt(registry.key(), clientRawId);
    }

    public static ResourceLocation serverIdToClient(Registry<?> registry, ResourceLocation serverId) {
        return serverIdToClient(ConnectionInfo.protocolVersion, registry, serverId);
    }

    public static ResourceLocation serverIdToClient(int serverVersion, Registry<?> registry, ResourceLocation serverId) {
        return protocolClasses.get(serverVersion).remapSResourceLocation(registry.key(), serverId);
    }

    public static ResourceLocation clientIdToServer(Registry<?> registry, ResourceLocation clientId) {
        return clientIdToServer(ConnectionInfo.protocolVersion, registry, clientId);
    }

    public static ResourceLocation clientIdToServer(int serverVersion, Registry<?> registry, ResourceLocation clientId) {
        return protocolClasses.get(serverVersion).remapCResourceLocation(registry.key(), clientId);
    }

    @Nullable
    public static Integer serverIdToRawId(Registry<?> registry, ResourceLocation serverId) {
        return serverIdToRawId(ConnectionInfo.protocolVersion, registry, serverId);
    }

    @Nullable
    public static <T> Integer serverIdToRawId(int serverVersion, Registry<T> registry, ResourceLocation serverId) {
        ResourceLocation clientId = serverIdToClient(serverVersion, registry, serverId);
        T value = registry.get(clientId);
        if (value == null) {
            return null;
        }
        int clientRawId = registry.getId(value);
        return clientRawIdToServer(serverVersion, registry, clientRawId);
    }

    @Nullable
    public static ResourceLocation serverRawIdToId(Registry<?> registry, int serverRawId) {
        return serverRawIdToId(ConnectionInfo.protocolVersion, registry, serverRawId);
    }

    @Nullable
    public static <T> ResourceLocation serverRawIdToId(int serverVersion, Registry<T> registry, int serverRawId) {
        int clientRawId = serverRawIdToClient(serverVersion, registry, serverRawId);
        T value = registry.byId(clientRawId);
        if (value == null) {
            return null;
        }
        ResourceLocation clientId = registry.getKey(value);
        assert clientId != null;
        return clientIdToServer(serverVersion, registry, clientId);
    }

    public static int serverBlockStateIdToClient(int serverBlockStateId) {
        return serverBlockStateIdToClient(ConnectionInfo.protocolVersion, serverBlockStateId);
    }

    public static int serverBlockStateIdToClient(int serverVersion, int serverBlockStateId) {
        return protocolClasses.get(serverVersion).remapSIntBlockState(serverBlockStateId);
    }

    public static int clientBlockStateIdToServer(int clientBlockStateId) {
        return clientBlockStateIdToServer(ConnectionInfo.protocolVersion, clientBlockStateId);
    }

    public static int clientBlockStateIdToServer(int serverVersion, int clientBlockStateId) {
        return protocolClasses.get(serverVersion).remapCIntBlockState(clientBlockStateId);
    }

    private static final ReadWriteLock blockStateRegistryBitsLock = new ReentrantReadWriteLock();
    private static final Int2IntMap blockStateRegistryBits = Int2IntMaps.synchronize(new Int2IntOpenHashMap());

    public static int getServerBlockStateRegistryBits() {
        blockStateRegistryBitsLock.readLock().lock();
        try {
            int result = blockStateRegistryBits.getOrDefault(ConnectionInfo.protocolVersion, -1);
            if (result != -1) {
                return -1;
            }
        } finally {
            blockStateRegistryBitsLock.readLock().unlock();
        }
        blockStateRegistryBitsLock.writeLock().lock();
        try {
            return blockStateRegistryBits.computeIfAbsent(ConnectionInfo.protocolVersion, k -> {
                int count = 0;
                for (BlockState state : Block.BLOCK_STATE_REGISTRY) {
                    if (doesServerKnowBlockState(state)) {
                        count++;
                    }
                }
                return Mth.ceillog2(count);
            });
        } finally {
            blockStateRegistryBitsLock.writeLock().unlock();
        }
    }

    private static final Map<ResourceLocation, OptionalInt> versionRemovedCache = new HashMap<>();
    private static final ReadWriteLock versionRemovedCacheLock = new ReentrantReadWriteLock();

    /**
     * Returns the last version with the specified ID instead of the multiconnect substitute.
     */
    @Nullable
    public static Integer getVersionRemoved(Registry<?> registry, ResourceLocation id) {
        ResourceLocation clientId = serverIdToClient(registry, id);
        if (clientId == null || !"multiconnect".equals(clientId.getNamespace())) {
            return null;
        }

        versionRemovedCacheLock.readLock().lock();
        try {
            OptionalInt versionRemoved = versionRemovedCache.get(clientId);
            //noinspection OptionalAssignedToNull
            if (versionRemoved != null) {
                return versionRemoved.isPresent() ? versionRemoved.getAsInt() : null;
            }
        } finally {
            versionRemovedCacheLock.readLock().unlock();
        }
        versionRemovedCacheLock.writeLock().lock();
        try {
            OptionalInt versionRemoved = versionRemovedCache.get(clientId);
            //noinspection OptionalAssignedToNull
            if (versionRemoved != null) {
                return versionRemoved.isPresent() ? versionRemoved.getAsInt() : null;
            }

            Integer result = null;
            for (ConnectionMode protocol : ConnectionMode.protocolValues()) {
                ResourceLocation versionId = clientIdToServer(protocol.getValue(), registry, clientId);
                if (versionId == null) {
                    break;
                }
                if (!"multiconnect".equals(versionId.getNamespace())) {
                    result = protocol.getValue();
                    break;
                }
            }
            versionRemovedCache.put(clientId, result != null ? OptionalInt.of(result) : OptionalInt.empty());
            return result;
        } finally {
            versionRemovedCacheLock.writeLock().unlock();
        }
    }

    public static class Internals {
        private static final LoadingCache<ByteBuf, TypedMap> bufUserData = CacheBuilder.newBuilder().weakKeys().build(CacheLoader.from(TypedMap::new));

        public static PacketIntrinsics.StartSendPacketResult translateSPacket(int protocol, ByteBuf buf) {
            return protocolClasses.get(protocol).translateSPacket(buf);
        }

        public static PacketIntrinsics.StartSendPacketResult translateCPacket(int protocol, ByteBuf buf) {
            return protocolClasses.get(protocol).translateCPacket(buf);
        }

        public static void setUserData(Packet<?> packet, TypedMap userData) {
            packetUserData.put(packet, userData);
        }

        public static TypedMap getUserData(ByteBuf buf) {
            return bufUserData.getUnchecked(buf);
        }

        public static void setUserData(ByteBuf buf, TypedMap userData) {
            bufUserData.put(buf, userData);
        }

        public static void submitTranslationTask(
                Class<?>[] readDependencies,
                Class<?>[] writeDependencies,
                Runnable translation,
                Runnable onTranslated,
                boolean clientbound
        ) {
            ReadWritePacketExecutor executor = clientbound ? clientboundExecutor : serverboundExecutor;

            if (!USE_THREADED_TRANSLATION || executor == null) {
                translation.run();
                onTranslated.run();
                return;
            }

            executor.submit(readDependencies, writeDependencies, translation, onTranslated);
        }

        public static Map<Class<?>, Object> getGlobalData() {
            return globalData;
        }
    }

    private static MethodHandle findMethodHandle(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... paramTypes) {
        try {
            return MethodHandles.lookup().findStatic(clazz, methodName, MethodType.methodType(returnType, paramTypes));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Class " + clazz.getName() + " is missing required method " + returnType.getName() + " " + methodName + " " + Arrays.stream(paramTypes).map(Class::getName).collect(Collectors.joining(", ", "(", ")")));
        }
    }

    private static class ProtocolClassProxy {
        private final MethodHandle translateSPacket;
        private final MethodHandle translateCPacket;
        private final MethodHandle sendToClient;
        private final MethodHandle sendToServer;
        private final MethodHandle doesServerKnow;
        private final MethodHandle doesServerKnowMulticonnect;
        private final MethodHandle doesServerKnowBlockState;
        private final MethodHandle doesServerKnowBlockStateMulticonnect;
        private final MethodHandle remapCInt;
        private final MethodHandle remapSInt;
        private final MethodHandle remapCResourceLocation;
        private final MethodHandle remapSResourceLocation;
        private final MethodHandle remapCIntBlockState;
        private final MethodHandle remapSIntBlockState;

        ProtocolClassProxy(Class<?> clazz, int protocol) {
            this.translateSPacket = findMethodHandle(clazz, "translateSPacket", PacketIntrinsics.StartSendPacketResult.class, ByteBuf.class);
            this.translateCPacket = findMethodHandle(clazz, "translateCPacket", PacketIntrinsics.StartSendPacketResult.class, ByteBuf.class);
            this.sendToClient = findMethodHandle(clazz, "sendToClient", void.class, Object.class, List.class, ClientPacketListener.class, Map.class, TypedMap.class);
            this.sendToServer = findMethodHandle(clazz, "sendToServer", void.class, Object.class, int.class, List.class, ClientPacketListener.class, Map.class, TypedMap.class);
            this.doesServerKnow = findMethodHandle(clazz, "doesServerKnow", boolean.class, ResourceKey.class, int.class);
            this.doesServerKnowMulticonnect = findMethodHandle(clazz, "doesServerKnowMulticonnect", boolean.class, ResourceKey.class);
            this.doesServerKnowBlockState = findMethodHandle(clazz, "doesServerKnowBlockState", boolean.class, int.class);
            this.doesServerKnowBlockStateMulticonnect = findMethodHandle(clazz, "doesServerKnowBlockStateMulticonnect", boolean.class, BlockState.class);
            this.remapCInt = findMethodHandle(clazz, "remapCInt", int.class, ResourceKey.class, int.class);
            this.remapSInt = findMethodHandle(clazz, "remapSInt", int.class, ResourceKey.class, int.class);
            this.remapCResourceLocation = findMethodHandle(clazz, "remapCResourceLocation", ResourceLocation.class, ResourceKey.class, ResourceLocation.class);
            this.remapSResourceLocation = findMethodHandle(clazz, "remapSResourceLocation", ResourceLocation.class, ResourceKey.class, ResourceLocation.class);
            this.remapCIntBlockState = findMethodHandle(clazz, "remapCIntBlockState", int.class, int.class);
            this.remapSIntBlockState = findMethodHandle(clazz, "remapSIntBlockState", int.class, int.class);
        }

        PacketIntrinsics.StartSendPacketResult translateSPacket(ByteBuf buf) {
            try {
                return (PacketIntrinsics.StartSendPacketResult) translateSPacket.invoke(buf);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        PacketIntrinsics.StartSendPacketResult translateCPacket(ByteBuf buf) {
            try {
                return (PacketIntrinsics.StartSendPacketResult) translateCPacket.invoke(buf);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        void sendToClient(Object packet, List<ByteBuf> outBufs, ClientPacketListener networkHandler, Map<Class<?>, Object> globalData, TypedMap userData) {
            try {
                sendToClient.invoke(packet, outBufs, networkHandler, globalData, userData);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        void sendToServer(Object packet, int fromProtocol, List<ByteBuf> outBufs, ClientPacketListener networkHandler, Map<Class<?>, Object> globalData, TypedMap userData) {
            try {
                sendToServer.invoke(packet, fromProtocol, outBufs, networkHandler, globalData, userData);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        boolean doesServerKnow(ResourceKey<? extends Registry<?>> registry, int newId) {
            try {
                return (Boolean) doesServerKnow.invoke(registry, newId);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        boolean doesServerKnowMulticonnect(ResourceKey<?> value) {
            try {
                return (Boolean) doesServerKnowMulticonnect.invoke(value);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        boolean doesServerKnowBlockState(int newId) {
            try {
                return (Boolean) doesServerKnowBlockState.invoke(newId);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        boolean doesServerKnowBlockStateMulticonnect(BlockState value) {
            try {
                return (Boolean) doesServerKnowBlockStateMulticonnect.invoke(value);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        int remapCInt(ResourceKey<? extends Registry<?>> registry, int value) {
            try {
                return (Integer) remapCInt.invoke(registry, value);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        int remapSInt(ResourceKey<? extends Registry<?>> registry, int value) {
            try {
                return (Integer) remapSInt.invoke(registry, value);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        ResourceLocation remapCResourceLocation(ResourceKey<? extends Registry<?>> registry, ResourceLocation value) {
            try {
                return (ResourceLocation) remapCResourceLocation.invoke(registry, value);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        ResourceLocation remapSResourceLocation(ResourceKey<? extends Registry<?>> registry, ResourceLocation value) {
            try {
                return (ResourceLocation) remapSResourceLocation.invoke(registry, value);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        int remapCIntBlockState(int value) {
            try {
                return (Integer) remapCIntBlockState.invoke(value);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        int remapSIntBlockState(int value) {
            try {
                return (Integer) remapSIntBlockState.invoke(value);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }
    }
}
