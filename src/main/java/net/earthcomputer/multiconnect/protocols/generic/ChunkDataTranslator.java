package net.earthcomputer.multiconnect.protocols.generic;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.dimension.DimensionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChunkDataTranslator {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(
            Math.max(1, Runtime.getRuntime().availableProcessors() - 1),
            new ThreadFactoryBuilder().setNameFormat("multiconnect chunk translator #%d").build()
    );
    private static final ThreadLocal<ChunkDataTranslator> CURRENT_TRANSLATOR = new ThreadLocal<>();

    public static ChunkDataTranslator current() {
        return CURRENT_TRANSLATOR.get();
    }

    private final ChunkDataS2CPacket packet;
    private final DimensionType dimension;
    private final DynamicRegistryManager registryManager;
    private final List<Packet<ClientPlayPacketListener>> postPackets = new ArrayList<>();
    private final Map<String, Object> userData = new HashMap<>();

    public ChunkDataTranslator(ChunkDataS2CPacket packet, DimensionType dimension, DynamicRegistryManager registryManager) {
        this.packet = packet;
        this.dimension = dimension;
        this.registryManager = registryManager;
    }

    public static void submit(ChunkDataS2CPacket packet) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.world != null;
        ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
        assert networkHandler != null;
        ChunkDataTranslator translator = new ChunkDataTranslator(packet, mc.world.getDimension(), networkHandler.getRegistryManager());
        EXECUTOR.submit(() -> {
            CURRENT_TRANSLATOR.set(translator);

            TransformerByteBuf buf = new TransformerByteBuf(packet.getReadBuffer(), null);
            buf.readTopLevelType(ChunkData.class);
            ChunkData chunkData = ChunkData.read(buf);
            ConnectionInfo.protocol.postTranslateChunk(translator, chunkData);
            ((IChunkDataS2CPacket) packet).setData(chunkData.toByteArray());

            CURRENT_TRANSLATOR.set(null);

            ((IChunkDataS2CPacket) packet).multiconnect_setDataTranslated(true);
            networkHandler.onChunkData(packet);

            for (Packet<ClientPlayPacketListener> postPacket : translator.postPackets) {
                postPacket.apply(networkHandler);
            }
        });
    }

    public ChunkDataS2CPacket getPacket() {
        return packet;
    }

    public DimensionType getDimension() {
        return dimension;
    }

    public DynamicRegistryManager getRegistryManager() {
        return registryManager;
    }

    public List<Packet<ClientPlayPacketListener>> getPostPackets() {
        return postPackets;
    }

    public Object getUserData(String key) {
        return userData.get(key);
    }

    public void setUserData(String key, Object value) {
        userData.put(key, value);
    }
}
