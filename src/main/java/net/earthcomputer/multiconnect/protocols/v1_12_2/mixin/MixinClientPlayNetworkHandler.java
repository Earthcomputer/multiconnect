package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.*;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.*;
import net.minecraft.tags.NetworkTagCollection;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(ClientPlayNetHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Shadow private Minecraft client;

    @Shadow public abstract void handleTags(STagsListPacket packet);

    @Shadow public abstract void handleUpdateRecipes(SUpdateRecipesPacket packet);

    @Shadow public abstract void handleCommandList(SCommandListPacket packet);

    @Inject(method = "handleJoinGame", at = @At("RETURN"))
    private void onOnGameJoin(SJoinGamePacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            NetworkTagManager tagManager = new NetworkTagManager();
            //noinspection ConstantConditions
            RegistryTagManagerAccessor tagManagerAccessor = (RegistryTagManagerAccessor) tagManager;
            Protocol_1_12_2 protocol = (Protocol_1_12_2) ConnectionInfo.protocol;
            toTagContainer(tagManagerAccessor.getBlocks(), protocol.getBlockTags());
            toTagContainer(tagManagerAccessor.getItems(), protocol.getItemTags());
            toTagContainer(tagManagerAccessor.getFluids(), protocol.getFluidTags());
            toTagContainer(tagManagerAccessor.getEntityTypes(), protocol.getEntityTypeTags());
            handleTags(new STagsListPacket(tagManager));

            List<IRecipe<?>> recipes = new ArrayList<>();
            List<RecipeInfo<?>> recipeInfos = protocol.getCraftingRecipes();
            for (int i = 0; i < recipeInfos.size(); i++) {
                recipes.add(recipeInfos.get(i).create(new ResourceLocation(String.valueOf(i))));
            }
            handleUpdateRecipes(new SUpdateRecipesPacket(recipes));

            CommandDispatcher<ISuggestionProvider> dispatcher = new CommandDispatcher<>();
            Commands_1_12_2.register(dispatcher, null);
            handleCommandList(new SCommandListPacket(dispatcher.getRoot()));
            TabCompletionManager.requestCommandList();
        }
    }

    @SuppressWarnings("unchecked")
    @Unique
    private <T> void toTagContainer(NetworkTagCollection<T> container, Multimap<Tag<T>, T> tags) {
        ImmutableMap.Builder<ResourceLocation, Tag<T>> map = new ImmutableMap.Builder<>();
        for (Map.Entry<Tag<T>, Collection<T>> entry : tags.asMap().entrySet()) {
            ResourceLocation id = entry.getKey().getId();
            Tag.Builder<T> tag = Tag.Builder.create();
            entry.getValue().forEach(tag::add);
            map.put(id, tag.build(id));
        }
        ((TagContainerAccessor<T>) container).multiconnect_setEntries(map.build());
    }

    @Inject(method = "handleUpdateTileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/SUpdateTileEntityPacket;getTileEntityType()I"))
    private void onOnBlockEntityUpdate(SUpdateTileEntityPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            assert client.world != null;
            TileEntity be = client.world.getTileEntity(packet.getPos());
            if (packet.getTileEntityType() == 5 && be instanceof FlowerPotBlockEntity) {
                be.read(packet.getNbtCompound());
            }
        }
    }

    @Inject(method = "handleTabComplete", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/IPacket;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/concurrent/ThreadTaskExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onOnCommandSuggestions(STabCompletePacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            if (TabCompletionManager.handleCustomCompletions(packet))
                ci.cancel();
        }
    }

    @Inject(method = "handleEntityStatus", at = @At("RETURN"))
    private void onOnEntityStatus(SEntityStatusPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            assert Minecraft.getInstance().world != null;
            if (packet.getEntity(Minecraft.getInstance().world) == Minecraft.getInstance().player
                    && packet.getOpCode() >= 24 && packet.getOpCode() <= 28) {
                TabCompletionManager.requestCommandList();
            }
        }
    }

}
