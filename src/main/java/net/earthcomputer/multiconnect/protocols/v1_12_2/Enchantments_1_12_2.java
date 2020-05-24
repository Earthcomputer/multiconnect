package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;

import static net.minecraft.enchantment.Enchantments.*;

public class Enchantments_1_12_2 {

    private static void register(ISimpleRegistry<Enchantment> registry, Enchantment enchantment, int id, String name) {
        RegistryKey<Enchantment> key = RegistryKey.of(registry.getRegistryKey(), new Identifier(name));
        registry.register(enchantment, id, key, false);
    }

    public static void registerEnchantments(ISimpleRegistry<Enchantment> registry) {
        registry.clear(false);
        register(registry, PROTECTION, 0, "protection");
        register(registry, FIRE_PROTECTION, 1, "fire_protection");
        register(registry, FEATHER_FALLING, 2, "feather_falling");
        register(registry, BLAST_PROTECTION, 3, "blast_protection");
        register(registry, PROJECTILE_PROTECTION, 4, "projectile_protection");
        register(registry, RESPIRATION, 5, "respiration");
        register(registry, AQUA_AFFINITY, 6, "aqua_affinity");
        register(registry, THORNS, 7, "thorns");
        register(registry, DEPTH_STRIDER, 8, "depth_strider");
        register(registry, FROST_WALKER, 9, "frost_walker");
        register(registry, BINDING_CURSE, 10, "binding_curse");
        register(registry, SHARPNESS, 16, "sharpness");
        register(registry, SMITE, 17, "smite");
        register(registry, BANE_OF_ARTHROPODS, 18, "bane_of_arthropods");
        register(registry, KNOCKBACK, 19, "knockback");
        register(registry, FIRE_ASPECT, 20, "fire_aspect");
        register(registry, LOOTING, 21, "looting");
        register(registry, SWEEPING, 22, "sweeping");
        register(registry, EFFICIENCY, 32, "efficiency");
        register(registry, SILK_TOUCH, 33, "silk_touch");
        register(registry, UNBREAKING, 34, "unbreaking");
        register(registry, FORTUNE, 35, "fortune");
        register(registry, POWER, 48, "power");
        register(registry, PUNCH, 49, "punch");
        register(registry, FLAME, 50, "flame");
        register(registry, INFINITY, 51, "infinity");
        register(registry, LUCK_OF_THE_SEA, 61, "luck_of_the_sea");
        register(registry, LURE, 62, "lure");
        register(registry, MENDING, 70, "mending");
        register(registry, VANISHING_CURSE, 71, "vanishing_curse");
    }

}
