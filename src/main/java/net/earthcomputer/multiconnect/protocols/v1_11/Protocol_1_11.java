package net.earthcomputer.multiconnect.protocols.v1_11;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.impl.RegistryMutator;
import net.earthcomputer.multiconnect.protocols.v1_11.mixin.PigEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_11_2.Protocol_1_11_2;
import net.earthcomputer.multiconnect.protocols.v1_12_2.RecipeInfo;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class Protocol_1_11 extends Protocol_1_11_2 {
    @Override
    public void mutateRegistries(RegistryMutator mutator) {
        super.mutateRegistries(mutator);
        mutator.mutate(Protocols.V1_11, Registry.ITEM, this::mutateItemRegistry);
        mutator.mutate(Protocols.V1_11, Registry.ENCHANTMENT, this::mutateEnchantmentRegistry);
    }

    private void mutateItemRegistry(ISimpleRegistry<Item> registry) {
        registry.purge(Items.IRON_NUGGET);
        rename(registry, Items.TOTEM_OF_UNDYING, "totem");
    }

    private void mutateEnchantmentRegistry(ISimpleRegistry<Enchantment> registry) {
        registry.purge(Enchantments.SWEEPING);
    }

    // TODO: fishing stats

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == FireworkRocketEntity.class && data == Protocol_1_13_2.OLD_FIREWORK_SHOOTER) {
            return false;
        }
        if (clazz == PigEntity.class && data == PigEntityAccessor.getBoostTime()) {
            return false;
        }
        return super.acceptEntityData(clazz, data);
    }

    @Override
    public List<RecipeInfo<?>> getCraftingRecipes() {
        List<RecipeInfo<?>> recipes = super.getCraftingRecipes();
        recipes.removeIf(recipe -> recipe.getOutput().getItem() == Items.IRON_NUGGET);
        recipes.removeIf(recipe -> recipe.getDistinguisher().equals("iron_nugget_to_ingot"));
        return recipes;
    }
}
