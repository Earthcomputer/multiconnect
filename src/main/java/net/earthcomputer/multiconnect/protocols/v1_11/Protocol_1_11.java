package net.earthcomputer.multiconnect.protocols.v1_11;

import net.earthcomputer.multiconnect.protocols.v1_11.mixin.PigEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_11_2.Protocol_1_11_2;
import net.earthcomputer.multiconnect.protocols.v1_12_2.RecipeInfo;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import java.util.List;

public class Protocol_1_11 extends Protocol_1_11_2 {
    public static final Identifier JUNK_FISHED = new Identifier("junk_fished");
    public static final Identifier TREASURE_FISHED = new Identifier("treasure_fished");

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
