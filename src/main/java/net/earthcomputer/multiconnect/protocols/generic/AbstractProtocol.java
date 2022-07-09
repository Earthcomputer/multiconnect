package net.earthcomputer.multiconnect.protocols.generic;

import com.google.common.base.Suppliers;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.mixin.bridge.MinecraftAccessor;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.BlockConnections;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.BlockConnector;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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

    public void setup() {
        PacketSystem.connect();
        revertCollisionBoxes();
        SynchedDataManager.onConnectToServer();
        AssetDownloader.reloadLanguages();
        markChangedCollisionBoxes();
        ((MinecraftAccessor) Minecraft.getInstance()).getSearchRegistry().onResourceManagerReload(Minecraft.getInstance().getResourceManager());
    }

    public void disable() {
        PacketSystem.disconnect();
    }

    public void preAcceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
    }

    public boolean acceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        return true;
    }

    protected void markChangedCollisionBoxes() {
    }

    private void revertCollisionBoxes() {
        for (Block block : collisionBoxesToRevert) {
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                state.initCache();
            }
        }
        collisionBoxesToRevert.clear();
    }

    protected void markCollisionBoxChanged(Block block) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            state.initCache();
        }
        collisionBoxesToRevert.add(block);
    }

    public BlockState getActualState(Level world, BlockPos pos, BlockState state) {
        return state;
    }

    public void postEntityDataRegister(Class<? extends Entity> clazz) {
    }

    public <T> T readEntityData(EntityDataSerializer<T> handler, FriendlyByteBuf buf) {
        return handler.read(buf);
    }

    @ThreadSafe
    public BlockConnector getBlockConnector() {
        return lazyBlockConnector.get();
    }

    public boolean shouldBlockChangeReplaceBlockEntity(Block oldBlock, Block newBlock) {
        return oldBlock != newBlock;
    }

    public float getBlockDestroySpeed(BlockState state, float destroySpeed) {
        return destroySpeed;
    }

    public float getBlockExplosionResistance(Block block, float resistance) {
        return resistance;
    }

    public int clientSlotIdToServer(AbstractContainerMenu menu, int slotId) {
        return slotId;
    }

    public int serverSlotIdToClient(AbstractContainerMenu menu, int slotId) {
        return slotId;
    }
}
