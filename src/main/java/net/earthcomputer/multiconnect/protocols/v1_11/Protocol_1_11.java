package net.earthcomputer.multiconnect.protocols.v1_11;

import net.earthcomputer.multiconnect.protocols.v1_11.mixin.PigAccessor;
import net.earthcomputer.multiconnect.protocols.v1_12.RecipeInfo;
import net.earthcomputer.multiconnect.protocols.v1_13.Protocol_1_13_2;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.Items;
import java.util.List;

public class Protocol_1_11 extends Protocol_1_11_2 {
    public static final ResourceLocation JUNK_FISHED = new ResourceLocation("junk_fished");
    public static final ResourceLocation TREASURE_FISHED = new ResourceLocation("treasure_fished");

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        if (clazz == FireworkRocketEntity.class && data == Protocol_1_13_2.OLD_FIREWORK_SHOOTER) {
            return false;
        }
        if (clazz == Pig.class && data == PigAccessor.getDataBoostTime()) {
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
