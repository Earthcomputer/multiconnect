package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tag.RegistryTagContainer;
import net.minecraft.tag.RegistryTagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RegistryTagManager.class)
public interface RegistryTagManagerAccessor {

    @Accessor
    RegistryTagContainer<Block> getBlocks();

    @Accessor
    RegistryTagContainer<Item> getItems();

    @Accessor
    RegistryTagContainer<Fluid> getFluids();

    @Accessor
    RegistryTagContainer<EntityType<?>> getEntityTypes();
}
