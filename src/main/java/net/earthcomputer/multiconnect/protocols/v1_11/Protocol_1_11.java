package net.earthcomputer.multiconnect.protocols.v1_11;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.protocols.generic.RegistryBuilder;
import net.earthcomputer.multiconnect.protocols.generic.RegistryMutator;
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
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class Protocol_1_11 extends Protocol_1_11_2 {

    public static final Identifier JUNK_FISHED = new Identifier("junk_fished");
    public static final Identifier TREASURE_FISHED = new Identifier("treasure_fished");

    @Override
    public void mutateRegistries(RegistryMutator mutator) {
        super.mutateRegistries(mutator);
        mutator.mutate(Protocols.V1_11, Registry.ITEM_KEY, this::mutateItemRegistry);
        mutator.mutate(Protocols.V1_11, Registry.ENCHANTMENT_KEY, this::mutateEnchantmentRegistry);
        mutator.mutate(Protocols.V1_11, Registry.CUSTOM_STAT_KEY, this::mutateCustomStatRegistry);
    }

    private void mutateItemRegistry(RegistryBuilder<Item> registry) {
        registry.purge(Items.IRON_NUGGET);
        registry.rename(Items.TOTEM_OF_UNDYING, "totem");
    }

    private void mutateEnchantmentRegistry(RegistryBuilder<Enchantment> registry) {
        registry.purge(Enchantments.SWEEPING);
    }

    private void mutateCustomStatRegistry(RegistryBuilder<Identifier> registry) {
        registry.register(registry.getNextId(), JUNK_FISHED, JUNK_FISHED);
        registry.register(registry.getNextId(), TREASURE_FISHED, TREASURE_FISHED);
        Stats.CUSTOM.getOrCreateStat(JUNK_FISHED, StatFormatter.DEFAULT);
        Stats.CUSTOM.getOrCreateStat(TREASURE_FISHED, StatFormatter.DEFAULT);
    }

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
    public List<RecipeInfo<?>> getRecipes() {
        List<RecipeInfo<?>> recipes = super.getRecipes();
        recipes.removeIf(recipe -> recipe.getOutput().getItem() == Items.IRON_NUGGET);
        recipes.removeIf(recipe -> recipe.getDistinguisher().equals("iron_nugget_to_ingot"));
        return recipes;
    }
}
