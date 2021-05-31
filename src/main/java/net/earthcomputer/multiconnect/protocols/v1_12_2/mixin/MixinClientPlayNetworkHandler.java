package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.*;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Shadow @Final private MinecraftClient client;

    @Shadow public abstract void onSynchronizeTags(SynchronizeTagsS2CPacket packet);

    @Shadow public abstract void onSynchronizeRecipes(SynchronizeRecipesS2CPacket packet);

    @Shadow public abstract void onCommandTree(CommandTreeS2CPacket packet);

    @Shadow @Final private RecipeManager recipeManager;

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void onOnGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            // multiconnect will automatically populate the tag manager with required tags
            onSynchronizeTags(new SynchronizeTagsS2CPacket(new HashMap<>()));

            Protocol_1_12_2 protocol = (Protocol_1_12_2) ConnectionInfo.protocol;
            List<Recipe<?>> recipes = new ArrayList<>();
            List<RecipeInfo<?>> recipeInfos = protocol.getRecipes();
            for (int i = 0; i < recipeInfos.size(); i++) {
                recipes.add(recipeInfos.get(i).create(new Identifier(String.valueOf(i))));
            }
            onSynchronizeRecipes(new SynchronizeRecipesS2CPacket(recipes));

            var dispatcher = new CommandDispatcher<CommandSource>();
            Commands_1_12_2.registerAll(dispatcher, null);
            onCommandTree(new CommandTreeS2CPacket(dispatcher.getRoot()));
            TabCompletionManager.requestCommandList();
        }
    }

    @Inject(method = "onUnlockRecipes", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void onOnUnlockRecipes(UnlockRecipesS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2 && packet.getAction() == UnlockRecipesS2CPacket.Action.INIT) {
            // ensure recipe lists are mutable
            UnlockRecipesS2CAccessor accessor = (UnlockRecipesS2CAccessor) packet;
            accessor.setRecipeIdsToInit(new ArrayList<>(packet.getRecipeIdsToInit()));
            accessor.setRecipeIdsToChange(new ArrayList<>(packet.getRecipeIdsToChange()));

            // add smelting recipes
            for (Recipe<?> recipe : recipeManager.values()) {
                if (recipe.getType() == RecipeType.SMELTING) {
                    if (!packet.getRecipeIdsToInit().contains(recipe.getId())) {
                        packet.getRecipeIdsToInit().add(recipe.getId());
                    }
                    if (!packet.getRecipeIdsToChange().contains(recipe.getId())) {
                        packet.getRecipeIdsToChange().add(recipe.getId());
                    }
                }
            }
        }
    }

    @Inject(method = "onBlockEntityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;getBlockEntityType()I"))
    private void onOnBlockEntityUpdate(BlockEntityUpdateS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            assert client.world != null;
            BlockEntity be = client.world.getBlockEntity(packet.getPos());
            if (packet.getBlockEntityType() == 5 && be instanceof FlowerPotBlockEntity) {
                be.readNbt(packet.getNbt());
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

}
