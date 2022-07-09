package net.earthcomputer.multiconnect.protocols.v1_9;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.earthcomputer.multiconnect.protocols.v1_12.Protocol_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12.RecipeInfo;
import net.earthcomputer.multiconnect.protocols.v1_12.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_9.mixin.EntityAccessor;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

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
    public void registerCommands(CommandDispatcher<SharedSuggestionProvider> dispatcher, @Nullable Set<String> serverCommands) {
        super.registerCommands(dispatcher, serverCommands);
        BrigadierRemover.of(dispatcher).get("teleport").remove();
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        if (clazz == Entity.class && data == EntityAccessor.getDataNoGravity()) {
            return false;
        }
        if (clazz == AreaEffectCloud.class) {
            if (data == Protocol_1_12_2.OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM1 || data == Protocol_1_12_2.OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM2) {
                return false;
            }
        }
        return super.acceptEntityData(clazz, data);
    }
}
