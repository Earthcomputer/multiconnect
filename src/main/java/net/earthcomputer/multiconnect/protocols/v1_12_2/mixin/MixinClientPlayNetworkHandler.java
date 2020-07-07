package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.*;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.command.CommandSource;
import net.minecraft.tag.RegistryTagManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Shadow private MinecraftClient client;
    @Shadow private ClientWorld world;

    @Shadow public abstract void onSynchronizeTags(SynchronizeTagsS2CPacket packet);

    @Shadow public abstract void onSynchronizeRecipes(SynchronizeRecipesS2CPacket packet);

    @Shadow public abstract void onCommandTree(CommandTreeS2CPacket packet);

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void onOnGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            onSynchronizeTags(new SynchronizeTagsS2CPacket(new RegistryTagManager()));

            Protocol_1_12_2 protocol = (Protocol_1_12_2) ConnectionInfo.protocol;
            List<Recipe<?>> recipes = new ArrayList<>();
            List<RecipeInfo<?>> recipeInfos = protocol.getCraftingRecipes();
            for (int i = 0; i < recipeInfos.size(); i++) {
                recipes.add(recipeInfos.get(i).create(new Identifier(String.valueOf(i))));
            }
            onSynchronizeRecipes(new SynchronizeRecipesS2CPacket(recipes));

            CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
            Commands_1_12_2.registerAll(dispatcher, null);
            onCommandTree(new CommandTreeS2CPacket(dispatcher.getRoot()));
            TabCompletionManager.requestCommandList();
        }
    }

    @Inject(method = "onBlockEntityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;getBlockEntityType()I"))
    private void onOnBlockEntityUpdate(BlockEntityUpdateS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            assert client.world != null;
            BlockEntity be = client.world.getBlockEntity(packet.getPos());
            if (packet.getBlockEntityType() == 5 && be instanceof FlowerPotBlockEntity) {
                be.fromTag(be.getCachedState(), packet.getCompoundTag());
            }
        }
    }

    @Inject(method = "onCommandSuggestions", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onOnCommandSuggestions(CommandSuggestionsS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            if (TabCompletionManager.handleCustomCompletions(packet))
                ci.cancel();
        }
    }

    @Inject(method = "onEntityStatus", at = @At("RETURN"))
    private void onOnEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            assert MinecraftClient.getInstance().world != null;
            if (packet.getEntity(MinecraftClient.getInstance().world) == MinecraftClient.getInstance().player
                    && packet.getStatus() >= 24 && packet.getStatus() <= 28) {
                TabCompletionManager.requestCommandList();
            }
        }
    }

    @ModifyVariable(method = "onChunkData", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
    private WorldChunk fixChunk(WorldChunk chunk, ChunkDataS2CPacket packet) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            if (chunk != null) {
                UpgradeData upgradeData = ChunkUpgrader.fixChunk(chunk);
                ((IUpgradableChunk) chunk).multiconnect_setClientUpgradeData(upgradeData);
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        WorldChunk otherChunk = world.getChunkManager().getChunk(packet.getX() + dx, packet.getZ() + dz, ChunkStatus.FULL, false);
                        if (otherChunk != null)
                            ((IUpgradableChunk) otherChunk).multiconnect_onNeighborLoaded();
                    }
                }
            }
        }
        return chunk;
    }

}
