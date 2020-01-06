package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tag.RegistryTagContainer;

public interface IRegistryTagManager {

    RegistryTagContainer<Block> getBlocks();

    RegistryTagContainer<Item> getItems();

    RegistryTagContainer<Fluid> getFluids();

    RegistryTagContainer<EntityType<?>> getEntityTypes();

}
