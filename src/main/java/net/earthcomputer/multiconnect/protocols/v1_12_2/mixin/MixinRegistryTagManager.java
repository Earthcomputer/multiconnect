package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.protocols.v1_12_2.IRegistryTagManager;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tag.RegistryTagContainer;
import net.minecraft.tag.RegistryTagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RegistryTagManager.class)
public abstract class MixinRegistryTagManager implements IRegistryTagManager {

    @Accessor
    @Override
    public abstract RegistryTagContainer<Block> getBlocks();

    @Accessor
    @Override
    public abstract RegistryTagContainer<Item> getItems();

    @Accessor
    @Override
    public abstract RegistryTagContainer<Fluid> getFluids();

    @Accessor
    @Override
    public abstract RegistryTagContainer<EntityType<?>> getEntityTypes();
}
