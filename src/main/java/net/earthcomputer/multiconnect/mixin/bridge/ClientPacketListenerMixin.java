package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.BlockConnections;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.ChunkConnector;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.IBlockConnectableChunk;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Consumer;

@Mixin(value = ClientPacketListener.class, priority = -1000)
public class ClientPacketListenerMixin {
    @Shadow private ClientLevel level;
    @Shadow @Final private Minecraft minecraft;

    @Unique private ClientboundLevelChunkWithLightPacket currentChunkPacket;

    @Inject(method = "handleLevelChunkWithLight", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", shift = At.Shift.AFTER))
    private void onHandleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
        currentChunkPacket = packet;
    }

    @Redirect(method = "updateLevelChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientChunkCache;replaceWithPacketData(IILnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/nbt/CompoundTag;Ljava/util/function/Consumer;)Lnet/minecraft/world/level/chunk/LevelChunk;"))
    private LevelChunk fixChunk(
            ClientChunkCache instance,
            int x,
            int z,
            FriendlyByteBuf buf,
            CompoundTag nbt,
            Consumer<net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData.BlockEntityTagOutput> blockEntityVisitor
    ) {
        LevelChunk chunk = instance.replaceWithPacketData(x, z, buf, nbt, blockEntityVisitor);
        if (ConnectionInfo.protocolVersion != SharedConstants.getCurrentVersion().getProtocolVersion()) {
            if (chunk != null && !Utils.isChunkEmpty(chunk)) {
                var blocksNeedingUpdate = PacketSystem.getUserData(currentChunkPacket).get(BlockConnections.BLOCKS_NEEDING_UPDATE_KEY);
                ChunkConnector chunkConnector = new ChunkConnector(chunk, ConnectionInfo.protocol.getBlockConnector(), blocksNeedingUpdate);
                ((IBlockConnectableChunk) chunk).multiconnect_setChunkConnector(chunkConnector);
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    ChunkAccess neighborChunk = level.getChunk(x + side.getStepX(),
                            z + side.getStepZ(), ChunkStatus.FULL, false);
                    if (neighborChunk != null) {
                        chunkConnector.onNeighborChunkLoaded(side);
                        ChunkConnector neighborConnector = ((IBlockConnectableChunk) neighborChunk).multiconnect_getChunkConnector();
                        if (neighborConnector != null) {
                            neighborConnector.onNeighborChunkLoaded(side.getOpposite());
                        }
                    }
                }
            }
        }

        return chunk;
    }

    @Inject(method = "method_34007", remap = false, at = @At("RETURN"))
    private void fixDeltaChunk(int flags, BlockPos pos, BlockState state, CallbackInfo ci) {
        ChunkAccess chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        if (chunk != null) {
            ChunkConnector connector = ((IBlockConnectableChunk) chunk).multiconnect_getChunkConnector();
            if (connector != null) {
                connector.onBlockChange(pos, state.getBlock(), true);
            }

            BlockState currentState = level.getBlockState(pos);
            BlockState newState = ConnectionInfo.protocol.getActualState(level, pos, currentState);
            if (newState != currentState) {
                level.setBlock(pos, newState, Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_LIGHT);
            }
        }
    }

    @ModifyVariable(method = "handleContainerSetSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", shift = At.Shift.AFTER), ordinal = 0, argsOnly = true)
    private ClientboundContainerSetSlotPacket modifySlotUpdatePacket(ClientboundContainerSetSlotPacket packet) {
        LocalPlayer player = minecraft.player;
        assert player != null;
        AbstractContainerMenu menu = player.containerMenu;
        if (packet.getContainerId() != menu.containerId) {
            return packet;
        }

        int slot = ConnectionInfo.protocol.serverSlotIdToClient(menu, packet.getSlot());
        if (slot != packet.getSlot()) {
            packet = new ClientboundContainerSetSlotPacket(packet.getContainerId(), packet.getStateId(), slot, packet.getItem());
        }

        return packet;
    }

    @ModifyVariable(method = "handleContainerContent", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", shift = At.Shift.AFTER), ordinal = 0, argsOnly = true)
    private ClientboundContainerSetContentPacket modifyInventoryPacket(ClientboundContainerSetContentPacket packet) {
        LocalPlayer player = minecraft.player;
        assert player != null;
        AbstractContainerMenu menu = player.containerMenu;
        if (packet.getContainerId() != menu.containerId) {
            return packet;
        }

        List<ItemStack> newStacks = new ArrayList<>(packet.getItems().size());
        boolean modified = false;
        for (int oldSlotId = 0; oldSlotId < packet.getItems().size(); oldSlotId++) {
            int newSlotId = ConnectionInfo.protocol.serverSlotIdToClient(menu, oldSlotId);
            while (newStacks.size() <= newSlotId) {
                newStacks.add(ItemStack.EMPTY);
            }
            newStacks.set(newSlotId, packet.getItems().get(oldSlotId));
            modified |= newSlotId != oldSlotId;
        }

        if (modified) {
            NonNullList<ItemStack> newContents = NonNullList.of(ItemStack.EMPTY, newStacks.toArray(ItemStack[]::new));
            packet = new ClientboundContainerSetContentPacket(packet.getContainerId(), packet.getStateId(), newContents, packet.getCarriedItem());
        }

        return packet;
    }

}
