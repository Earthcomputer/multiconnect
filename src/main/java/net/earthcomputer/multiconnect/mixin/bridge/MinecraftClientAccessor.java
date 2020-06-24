package net.earthcomputer.multiconnect.mixin.bridge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.search.SearchManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {

    @Invoker
    void callInitializeSearchableContainers();

    @Accessor
    SearchManager getSearchManager();

}
