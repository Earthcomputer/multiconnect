package net.earthcomputer.multiconnect.protocols.v1_11_2;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.v1_11_2.mixin.PlayerEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_12.Protocol_1_12;
import net.earthcomputer.multiconnect.protocols.v1_12_2.RecipeInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.GlazedTerracottaBlock;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;

import java.util.List;
import java.util.Set;

public class Protocol_1_11_2 extends Protocol_1_12 {
    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == PlayerEntity.class && (data == PlayerEntityAccessor.getLeftShoulderEntity() || data == PlayerEntityAccessor.getRightShoulderEntity())) {
            return false;
        }
        if (clazz == IllagerEntity.class && data == Protocol_1_13_2.OLD_ILLAGER_FLAGS) {
            return false;
        }
        return super.acceptEntityData(clazz, data);
    }

    @Override
    public List<RecipeInfo<?>> getRecipes() {
        List<RecipeInfo<?>> recipes = super.getRecipes();
        recipes.removeIf(recipe -> {
            if (recipe.getOutput().getItem() instanceof BlockItem block) {
                if (block.getBlock() instanceof ConcretePowderBlock || block.getBlock() instanceof GlazedTerracottaBlock) {
                    return true;
                }
            }
            return false;
        });
        return recipes;
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSource> dispatcher, Set<String> serverCommands) {
        super.registerCommands(dispatcher, serverCommands);

        BrigadierRemover.of(dispatcher).get("advancement").remove();
        BrigadierRemover.of(dispatcher).get("function").remove();
        BrigadierRemover.of(dispatcher).get("recipe").remove();
        BrigadierRemover.of(dispatcher).get("reload").remove();

        Commands_1_12_2.registerVanilla(dispatcher, serverCommands, "achievement", AchievementCommand::register);
    }
}
