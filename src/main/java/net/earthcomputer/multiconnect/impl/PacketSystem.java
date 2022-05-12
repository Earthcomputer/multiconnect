package net.earthcomputer.multiconnect.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.mixin.connect.ClientConnectionAccessor;
import net.earthcomputer.multiconnect.protocols.generic.TypedMap;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PacketSystem {
    private static final Logger LOGGER = LogManager.getLogger();

    private PacketSystem() {}

    private static final Int2ObjectMap<ProtocolClassProxy> protocolClasses = Util.make(new Int2ObjectOpenHashMap<>(), map -> {
        for (ConnectionMode protocol : ConnectionMode.protocolValues()) {
            Class<?> clazz;
            try {
                clazz = Class.forName("net.earthcomputer.multiconnect.generated.Protocol_" + protocol.getName().replace('.', '_'));
            } catch (ClassNotFoundException e) {
                // TODO: make this a hard error once the packet system is complete
                LOGGER.warn("Protocol class not found for {}", protocol.getName());
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
    // TODO: clear global data on disconnect
    private static final Map<Class<?>, Object> globalData = new HashMap<>();

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

    public static void sendToServer(ClientPlayNetworkHandler networkHandler, int protocol, Object packet) {
        Channel channel = ((ClientConnectionAccessor) networkHandler.getConnection()).getChannel();
        Runnable send = () -> {
            List<ByteBuf> bufs = new ArrayList<>(1);
            TypedMap userData = new TypedMap();
            protocolClasses.get(ConnectionInfo.protocolVersion).sendToServer(packet, protocol, bufs, networkHandler, globalData, userData);
            PacketIntrinsics.sendRawToServer(networkHandler, bufs);
        };
        if (channel.eventLoop().inEventLoop()) {
            send.run();
        } else {
            channel.eventLoop().execute(send);
        }
    }

    public static void sendToClient(ClientPlayNetworkHandler networkHandler, int protocol, Object packet) {
        Channel channel = ((ClientConnectionAccessor) networkHandler.getConnection()).getChannel();
        Runnable send = () -> {
            List<ByteBuf> bufs = new ArrayList<>(1);
            TypedMap userData = new TypedMap();
            protocolClasses.get(protocol).sendToClient(packet, bufs, networkHandler, globalData, userData);
            PacketIntrinsics.sendRawToClient(networkHandler, userData, bufs);
        };
        if (channel.eventLoop().inEventLoop()) {
            send.run();
        } else {
            channel.eventLoop().execute(send);
        }
    }

    public static <T> boolean doesServerKnow(Registry<T> registry, RegistryKey<T> key) {
        return doesServerKnow(registry, key, registry.get(key));
    }

    public static <T> boolean doesServerKnow(Registry<T> registry, T value) {
        Optional<RegistryKey<T>> key = registry.getKey(value);
        if (key.isEmpty()) {
            return false;
        }
        return doesServerKnow(registry, key.get(), value);
    }

    private static <T> boolean doesServerKnow(Registry<T> registry, RegistryKey<T> key, T value) {
        int rawId = registry.getRawId(value);
        ProtocolClassProxy proxy = protocolClasses.get(ConnectionInfo.protocolVersion);
        return proxy.doesServerKnow(registry.getKey(), rawId) || proxy.doesServerKnowMulticonnect(key);
    }

    public static int serverRawIdToClient(Registry<?> registry, int serverRawId) {
        return protocolClasses.get(ConnectionInfo.protocolVersion).remapSInt(registry.getKey(), serverRawId);
    }

    public static int clientRawIdToServer(Registry<?> registry, int clientRawId) {
        return protocolClasses.get(ConnectionInfo.protocolVersion).remapCInt(registry.getKey(), clientRawId);
    }

    public static Identifier serverIdToClient(Registry<?> registry, Identifier serverId) {
        return protocolClasses.get(ConnectionInfo.protocolVersion).remapSIdentifier(registry.getKey(), serverId);
    }

    public static Identifier clientIdToServer(Registry<?> registry, Identifier clientId) {
        return protocolClasses.get(ConnectionInfo.protocolVersion).remapCIdentifier(registry.getKey(), clientId);
    }

    @Nullable
    public static <T> Integer serverIdToRawId(Registry<T> registry, Identifier serverId) {
        Identifier clientId = serverIdToClient(registry, serverId);
        T value = registry.get(clientId);
        if (value == null) {
            return null;
        }
        int clientRawId = registry.getRawId(value);
        return clientRawIdToServer(registry, clientRawId);
    }

    @Nullable
    public static <T> Identifier serverRawIdToId(Registry<T> registry, int serverRawId) {
        int clientRawId = serverRawIdToClient(registry, serverRawId);
        T value = registry.get(clientRawId);
        if (value == null) {
            return null;
        }
        Identifier clientId = registry.getId(value);
        return clientIdToServer(registry, clientId);
    }

    public static int serverBlockStateIdToClient(int serverBlockStateId) {
        return protocolClasses.get(ConnectionInfo.protocolVersion).remapSIntBlockState(serverBlockStateId);
    }

    public static int clientBlockStateIdToServer(int clientBlockStateId) {
        return protocolClasses.get(ConnectionInfo.protocolVersion).remapCIntBlockState(clientBlockStateId);
    }

    public static class Internals {
        private static final LoadingCache<ByteBuf, TypedMap> bufUserData = CacheBuilder.newBuilder().weakKeys().build(CacheLoader.from(TypedMap::new));

        public static void translateSPacket(int protocol, ByteBuf buf, List<ByteBuf> outBufs, ClientPlayNetworkHandler networkHandler, TypedMap userData) {
            protocolClasses.get(protocol).translateSPacket(buf, outBufs, networkHandler, globalData, userData);
        }

        public static void translateCPacket(int protocol, ByteBuf buf, List<ByteBuf> outBufs, ClientPlayNetworkHandler networkHandler, TypedMap userData) {
            protocolClasses.get(protocol).translateCPacket(buf, outBufs, networkHandler, globalData, userData);
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
        private final MethodHandle remapCInt;
        private final MethodHandle remapSInt;
        private final MethodHandle remapCIdentifier;
        private final MethodHandle remapSIdentifier;
        private final MethodHandle remapCIntBlockState;
        private final MethodHandle remapSIntBlockState;

        ProtocolClassProxy(Class<?> clazz, int protocol) {
            this.translateSPacket = findMethodHandle(clazz, "translateSPacket", void.class, ByteBuf.class, List.class, ClientPlayNetworkHandler.class, Map.class, TypedMap.class);
            this.translateCPacket = findMethodHandle(clazz, "translateCPacket", void.class, ByteBuf.class, List.class, ClientPlayNetworkHandler.class, Map.class, TypedMap.class);
            this.sendToClient = findMethodHandle(clazz, "sendToClient", void.class, Object.class, List.class, ClientPlayNetworkHandler.class, Map.class, TypedMap.class);
            this.sendToServer = findMethodHandle(clazz, "sendToServer", void.class, Object.class, int.class, List.class, ClientPlayNetworkHandler.class, Map.class, TypedMap.class);
            this.doesServerKnow = findMethodHandle(clazz, "doesServerKnow", boolean.class, RegistryKey.class, int.class);
            this.doesServerKnowMulticonnect = findMethodHandle(clazz, "doesServerKnowMulticonnect", boolean.class, RegistryKey.class);
            this.remapCInt = findMethodHandle(clazz, "remapCInt", int.class, RegistryKey.class, int.class);
            this.remapSInt = findMethodHandle(clazz, "remapSInt", int.class, RegistryKey.class, int.class);
            this.remapCIdentifier = findMethodHandle(clazz, "remapCIdentifier", Identifier.class, RegistryKey.class, Identifier.class);
            this.remapSIdentifier = findMethodHandle(clazz, "remapSIdentifier", Identifier.class, RegistryKey.class, Identifier.class);
            this.remapCIntBlockState = findMethodHandle(clazz, "remapCIntBlockState", int.class, int.class);
            this.remapSIntBlockState = findMethodHandle(clazz, "remapSIntBlockState", int.class, int.class);
        }

        void translateSPacket(ByteBuf buf, List<ByteBuf> outBufs, ClientPlayNetworkHandler networkHandler, Map<Class<?>, Object> globalData, TypedMap userData) {
            try {
                translateSPacket.invoke(buf, outBufs, networkHandler, globalData, userData);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        void translateCPacket(ByteBuf buf, List<ByteBuf> outBufs, ClientPlayNetworkHandler networkHandler, Map<Class<?>, Object> globalData, TypedMap userData) {
            try {
                translateCPacket.invoke(buf, outBufs, networkHandler, globalData, userData);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        void sendToClient(Object packet, List<ByteBuf> outBufs, ClientPlayNetworkHandler networkHandler, Map<Class<?>, Object> globalData, TypedMap userData) {
            try {
                sendToClient.invoke(packet, outBufs, networkHandler, globalData, userData);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        void sendToServer(Object packet, int fromProtocol, List<ByteBuf> outBufs, ClientPlayNetworkHandler networkHandler, Map<Class<?>, Object> globalData, TypedMap userData) {
            try {
                sendToServer.invoke(packet, fromProtocol, outBufs, networkHandler, globalData, userData);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        boolean doesServerKnow(RegistryKey<? extends Registry<?>> registry, int newId) {
            try {
                return (Boolean) doesServerKnow.invoke(registry, newId);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        boolean doesServerKnowMulticonnect(RegistryKey<?> value) {
            try {
                return (Boolean) doesServerKnowMulticonnect.invoke(value);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        int remapCInt(RegistryKey<? extends Registry<?>> registry, int value) {
            try {
                return (Integer) remapCInt.invoke(registry, value);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        int remapSInt(RegistryKey<? extends Registry<?>> registry, int value) {
            try {
                return (Integer) remapSInt.invoke(registry, value);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        Identifier remapCIdentifier(RegistryKey<? extends Registry<?>> registry, Identifier value) {
            try {
                return (Identifier) remapCIdentifier.invoke(registry, value);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        Identifier remapSIdentifier(RegistryKey<? extends Registry<?>> registry, Identifier value) {
            try {
                return (Identifier) remapSIdentifier.invoke(registry, value);
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
