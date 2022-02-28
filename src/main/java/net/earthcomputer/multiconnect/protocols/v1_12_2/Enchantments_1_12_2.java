package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.earthcomputer.multiconnect.protocols.generic.RegistryBuilder;
import net.minecraft.enchantment.Enchantment;

import static net.minecraft.enchantment.Enchantments.*;

public class Enchantments_1_12_2 {

    public static void registerEnchantments(RegistryBuilder<Enchantment> registry) {
        registry.disableSideEffects();
        registry.clear();
        registry.registerInPlace(0, PROTECTION, "protection");
        registry.registerInPlace(1, FIRE_PROTECTION, "fire_protection");
        registry.registerInPlace(2, FEATHER_FALLING, "feather_falling");
        registry.registerInPlace(3, BLAST_PROTECTION, "blast_protection");
        registry.registerInPlace(4, PROJECTILE_PROTECTION, "projectile_protection");
        registry.registerInPlace(5, RESPIRATION, "respiration");
        registry.registerInPlace(6, AQUA_AFFINITY, "aqua_affinity");
        registry.registerInPlace(7, THORNS, "thorns");
        registry.registerInPlace(8, DEPTH_STRIDER, "depth_strider");
        registry.registerInPlace(9, FROST_WALKER, "frost_walker");
        registry.registerInPlace(10, BINDING_CURSE, "binding_curse");
        registry.registerInPlace(16, SHARPNESS, "sharpness");
        registry.registerInPlace(17, SMITE, "smite");
        registry.registerInPlace(18, BANE_OF_ARTHROPODS, "bane_of_arthropods");
        registry.registerInPlace(19, KNOCKBACK, "knockback");
        registry.registerInPlace(20, FIRE_ASPECT, "fire_aspect");
        registry.registerInPlace(21, LOOTING, "looting");
        registry.registerInPlace(22, SWEEPING, "sweeping");
        registry.registerInPlace(32, EFFICIENCY, "efficiency");
        registry.registerInPlace(33, SILK_TOUCH, "silk_touch");
        registry.registerInPlace(34, UNBREAKING, "unbreaking");
        registry.registerInPlace(35, FORTUNE, "fortune");
        registry.registerInPlace(48, POWER, "power");
        registry.registerInPlace(49, PUNCH, "punch");
        registry.registerInPlace(50, FLAME, "flame");
        registry.registerInPlace(51, INFINITY, "infinity");
        registry.registerInPlace(61, LUCK_OF_THE_SEA, "luck_of_the_sea");
        registry.registerInPlace(62, LURE, "lure");
        registry.registerInPlace(70, MENDING, "mending");
        registry.registerInPlace(71, VANISHING_CURSE, "vanishing_curse");
        registry.enableSideEffects();
    }

}
