package net.earthcomputer.multiconnect.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.mixin.connect.ClientConnectionAccessor;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        List<ByteBuf> bufs = new ArrayList<>(1);
        protocolClasses.get(ConnectionInfo.protocolVersion).sendToServer(packet, protocol, bufs);

        if (bufs.isEmpty()) {
            return;
        }

        ChannelHandlerContext context = ((ClientConnectionAccessor) networkHandler.getConnection()).getChannel()
                .pipeline()
                .context("encoder");

        for (ByteBuf buf : bufs) {
            context.write(buf);
        }
        context.flush();
    }

    public static void sendToClient(ClientPlayNetworkHandler networkHandler, int protocol, Object packet) {
        List<ByteBuf> bufs = new ArrayList<>(1);
        protocolClasses.get(protocol).sendToClient(packet, bufs);

        if (bufs.isEmpty()) {
            return;
        }

        ChannelHandlerContext context = ((ClientConnectionAccessor) networkHandler.getConnection()).getChannel()
                .pipeline()
                .context("decoder");

        for (ByteBuf buf : bufs) {
            context.fireChannelRead(buf);
        }
    }

    public static void translateSPacket(int protocol, ByteBuf buf, List<ByteBuf> outBufs) {
        protocolClasses.get(protocol).translateSPacket(buf, outBufs);
    }

    public static void translateCPacket(int protocol, ByteBuf buf, List<ByteBuf> outBufs) {
        protocolClasses.get(protocol).translateCPacket(buf, outBufs);
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
            this.translateSPacket = findMethodHandle(clazz, "translateSPacket", void.class, ByteBuf.class, List.class);
            this.translateCPacket = findMethodHandle(clazz, "translateCPacket", void.class, ByteBuf.class, List.class);
            this.sendToClient = findMethodHandle(clazz, "sendToClient", void.class, Object.class, List.class);
            this.sendToServer = findMethodHandle(clazz, "sendToServer", void.class, Object.class, int.class, List.class);
        }

        void translateSPacket(ByteBuf buf, List<ByteBuf> outBufs) {
            try {
                translateSPacket.invoke(buf, outBufs);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        void translateCPacket(ByteBuf buf, List<ByteBuf> outBufs) {
            try {
                translateCPacket.invoke(buf, outBufs);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        void sendToClient(Object packet, List<ByteBuf> outBufs) {
            try {
                sendToClient.invoke(packet, outBufs);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }

        void sendToServer(Object packet, int fromProtocol, List<ByteBuf> outBufs) {
            try {
                sendToServer.invoke(packet, fromProtocol, outBufs);
            } catch (Throwable e) {
                throw PacketIntrinsics.sneakyThrow(e);
            }
        }
    }
}
