package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.BlockConnections;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.ChunkConnector;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.IBlockConnectableChunk;
import net.earthcomputer.multiconnect.protocols.v1_14_4.IBiomeStorage_1_14_4;
import net.earthcomputer.multiconnect.protocols.v1_14_4.Protocol_1_14_4;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
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

@Mixin(value = ClientPlayNetworkHandler.class, priority = -1000)
public class MixinClientPlayNetworkHandler {
    @Shadow private ClientWorld world;
    @Shadow @Final private MinecraftClient client;

    @Unique private ChunkDataS2CPacket currentChunkPacket;

    @Inject(method = "onChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onOnChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        currentChunkPacket = packet;
    }

    @Redirect(method = "loadChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientChunkManager;loadChunkFromPacket(IILnet/minecraft/network/PacketByteBuf;Lnet/minecraft/nbt/NbtCompound;Ljava/util/function/Consumer;)Lnet/minecraft/world/chunk/WorldChunk;"))
    private WorldChunk fixChunk(
            ClientChunkManager instance,
            int x,
            int z,
            PacketByteBuf buf,
            NbtCompound nbt,
            Consumer<net.minecraft.network.packet.s2c.play.ChunkData.BlockEntityVisitor> blockEntityVisitor
    ) {
        WorldChunk chunk = instance.loadChunkFromPacket(x, z, buf, nbt, blockEntityVisitor);
        if (ConnectionInfo.protocolVersion != SharedConstants.getGameVersion().getProtocolVersion()) {
            if (chunk != null && !Utils.isChunkEmpty(chunk)) {
                var blocksNeedingUpdate = PacketSystem.getUserData(currentChunkPacket).get(BlockConnections.BLOCKS_NEEDING_UPDATE_KEY);
                ChunkConnector chunkConnector = new ChunkConnector(chunk, ConnectionInfo.protocol.getBlockConnector(), blocksNeedingUpdate);
                ((IBlockConnectableChunk) chunk).multiconnect_setChunkConnector(chunkConnector);
                for (Direction side : Direction.Type.HORIZONTAL) {
                    Chunk neighborChunk = world.getChunk(x + side.getOffsetX(),
                            z + side.getOffsetZ(), ChunkStatus.FULL, false);
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

        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_4) {
            if (chunk != null) {
                Biome[] biomeData = PacketSystem.getUserData(currentChunkPacket).get(Protocol_1_14_4.BIOME_DATA_KEY);
                if (biomeData != null) {
                    ((IBiomeStorage_1_14_4) chunk).multiconnect_setBiomeArray_1_14_4(biomeData);
                }
            }
        }
        return chunk;
    }

    @Inject(method = "method_34007", remap = false, at = @At("RETURN"))
    private void fixDeltaChunk(int flags, BlockPos pos, BlockState state, CallbackInfo ci) {
        Chunk chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        if (chunk != null) {
            ChunkConnector connector = ((IBlockConnectableChunk) chunk).multiconnect_getChunkConnector();
            if (connector != null) {
                connector.onBlockChange(pos, state.getBlock(), true);
            }

            BlockState currentState = world.getBlockState(pos);
            BlockState newState = ConnectionInfo.protocol.getActualState(world, pos, currentState);
            if (newState != currentState) {
                world.setBlockState(pos, newState, Block.NOTIFY_ALL | Block.FORCE_STATE | Block.SKIP_LIGHTING_UPDATES);
            }
        }
    }

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void onOnCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (packet.getChannel().equals(CustomPayloadHandler.DROP_ID)) {
            ci.cancel();
        } else if (ConnectionInfo.protocolVersion != SharedConstants.getGameVersion().getProtocolVersion()
                && !CustomPayloadHandler.VANILLA_CLIENTBOUND_CHANNELS.contains(packet.getChannel())) {
            NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, MinecraftClient.getInstance());
            CustomPayloadHandler.handleClientboundCustomPayload((ClientPlayNetworkHandler) (Object) this, packet);
            ci.cancel();
        }
    }

    @ModifyVariable(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), ordinal = 0, argsOnly = true)
    private ScreenHandlerSlotUpdateS2CPacket modifySlotUpdatePacket(ScreenHandlerSlotUpdateS2CPacket packet) {
        ClientPlayerEntity player = client.player;
        assert player != null;
        ScreenHandler screenHandler = player.currentScreenHandler;
        if (packet.getSyncId() != screenHandler.syncId) {
            return packet;
        }

        int slot = ConnectionInfo.protocol.serverSlotIdToClient(screenHandler, packet.getSlot());
        if (slot != packet.getSlot()) {
            packet = new ScreenHandlerSlotUpdateS2CPacket(packet.getSyncId(), packet.getRevision(), slot, packet.getItemStack());
        }

        return packet;
    }

    @ModifyVariable(method = "onInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), ordinal = 0, argsOnly = true)
    private InventoryS2CPacket modifyInventoryPacket(InventoryS2CPacket packet) {
        ClientPlayerEntity player = client.player;
        assert player != null;
        ScreenHandler screenHandler = player.currentScreenHandler;
        if (packet.getSyncId() != screenHandler.syncId) {
            return packet;
        }

        List<ItemStack> newStacks = new ArrayList<>(packet.getContents().size());
        boolean modified = false;
        for (int oldSlotId = 0; oldSlotId < packet.getContents().size(); oldSlotId++) {
            int newSlotId = ConnectionInfo.protocol.serverSlotIdToClient(screenHandler, oldSlotId);
            while (newStacks.size() <= newSlotId) {
                newStacks.add(ItemStack.EMPTY);
            }
            newStacks.set(newSlotId, packet.getContents().get(oldSlotId));
            modified |= newSlotId != oldSlotId;
        }

        if (modified) {
            DefaultedList<ItemStack> newContents = DefaultedList.copyOf(ItemStack.EMPTY, newStacks.toArray(ItemStack[]::new));
            packet = new InventoryS2CPacket(packet.getSyncId(), packet.getRevision(), newContents, packet.getCursorStack());
        }

        return packet;
    }

}
