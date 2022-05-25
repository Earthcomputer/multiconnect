package net.earthcomputer.multiconnect.protocols.generic;

import com.google.common.base.Suppliers;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.mixin.bridge.MinecraftClientAccessor;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.BlockConnections;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.BlockConnector;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Supplier;

public abstract class AbstractProtocol {
    private static final List<Block> collisionBoxesToRevert = new ArrayList<>();

    private int protocolVersion;
    private final Supplier<BlockConnector> lazyBlockConnector = Suppliers.memoize(() -> BlockConnections.buildConnector(protocolVersion));

    // To be called by ProtocolRegistry only!
    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public void setup(boolean resourceReload) {
        PacketSystem.connect();
        revertCollisionBoxes();
        if (!resourceReload) {
            DataTrackerManager.onConnectToServer();
        }
        if (!resourceReload) {
            AssetDownloader.reloadLanguages();
        }
        markChangedCollisionBoxes();
        ((MinecraftClientAccessor) MinecraftClient.getInstance()).callInitializeSearchableContainers();
        ((MinecraftClientAccessor) MinecraftClient.getInstance()).getSearchManager().reload(MinecraftClient.getInstance().getResourceManager());
    }

    public void disable() {
        PacketSystem.disconnect();
    }

    public void preAcceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
    }

    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        return true;
    }

    protected void markChangedCollisionBoxes() {
    }

    private void revertCollisionBoxes() {
        for (Block block : collisionBoxesToRevert) {
            for (BlockState state : block.getStateManager().getStates()) {
                state.initShapeCache();
            }
        }
        collisionBoxesToRevert.clear();
    }

    protected void markCollisionBoxChanged(Block block) {
        for (BlockState state : block.getStateManager().getStates()) {
            state.initShapeCache();
        }
        collisionBoxesToRevert.add(block);
    }

    public BlockState getActualState(World world, BlockPos pos, BlockState state) {
        return state;
    }

    public void postEntityDataRegister(Class<? extends Entity> clazz) {
    }

    public <T> T readTrackedData(TrackedDataHandler<T> handler, PacketByteBuf buf) {
        return handler.read(buf);
    }

    @ThreadSafe
    public BlockConnector getBlockConnector() {
        return lazyBlockConnector.get();
    }

    public boolean shouldBlockChangeReplaceBlockEntity(Block oldBlock, Block newBlock) {
        return oldBlock != newBlock;
    }

    public float getBlockHardness(BlockState state, float hardness) {
        return hardness;
    }

    public float getBlockResistance(Block block, float resistance) {
        return resistance;
    }

    public int clientSlotIdToServer(ScreenHandler screenHandler, int slotId) {
        return slotId;
    }

    public int serverSlotIdToClient(ScreenHandler screenHandler, int slotId) {
        return slotId;
    }

    @ThreadSafe
    public ChunkPos extractChunkPos(Class<? extends Packet<?>> packetClass, PacketByteBuf buf) {
        if (packetClass == ChunkDataS2CPacket.class) {
            return new ChunkPos(buf.readInt(), buf.readInt());
        } else {
            throw new IllegalArgumentException();
        }
    }
}
