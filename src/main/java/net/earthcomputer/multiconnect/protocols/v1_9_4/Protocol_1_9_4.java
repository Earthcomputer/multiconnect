package net.earthcomputer.multiconnect.protocols.v1_9_4;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.earthcomputer.multiconnect.protocols.v1_12_2.Protocol_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12_2.RecipeInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_9_4.mixin.EntityAccessor;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.item.Items;

import java.util.List;
import java.util.Set;

public class Protocol_1_9_4 extends Protocol_1_10 {
    @Override
    public List<RecipeInfo<?>> getRecipes() {
        List<RecipeInfo<?>> recipes = super.getRecipes();
        recipes.removeIf(recipe -> recipe.getOutput().getItem() == Items.BONE_BLOCK);
        return recipes;
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSource> dispatcher, Set<String> serverCommands) {
        super.registerCommands(dispatcher, serverCommands);
        BrigadierRemover.of(dispatcher).get("teleport").remove();
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == Entity.class && data == EntityAccessor.getNoGravity()) {
            return false;
        }
        if (clazz == AreaEffectCloudEntity.class) {
            if (data == Protocol_1_12_2.OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM1 || data == Protocol_1_12_2.OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM2) {
                return false;
            }
        }
        return super.acceptEntityData(clazz, data);
    }
}
