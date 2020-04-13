package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.NetworkTagCollection;
import net.minecraft.tags.NetworkTagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NetworkTagManager.class)
public interface RegistryTagManagerAccessor {

    @Accessor
    NetworkTagCollection<Block> getBlocks();

    @Accessor
    NetworkTagCollection<Item> getItems();

    @Accessor
    NetworkTagCollection<Fluid> getFluids();

    @Accessor
    NetworkTagCollection<EntityType<?>> getEntityTypes();
}
