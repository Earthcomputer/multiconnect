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
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        ProtocolClassProxy(Class<?> clazz, int protocol) {
            this.translateSPacket = findMethodHandle(clazz, "translateSPacket", void.class, ByteBuf.class, List.class, ClientPlayNetworkHandler.class, Map.class, TypedMap.class);
            this.translateCPacket = findMethodHandle(clazz, "translateCPacket", void.class, ByteBuf.class, List.class, ClientPlayNetworkHandler.class, Map.class, TypedMap.class);
            this.sendToClient = findMethodHandle(clazz, "sendToClient", void.class, Object.class, List.class, ClientPlayNetworkHandler.class, Map.class, TypedMap.class);
            this.sendToServer = findMethodHandle(clazz, "sendToServer", void.class, Object.class, int.class, List.class, ClientPlayNetworkHandler.class, Map.class, TypedMap.class);
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
    }
}
