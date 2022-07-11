package net.earthcomputer.multiconnect.protocols.v1_11;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.v1_11.mixin.PlayerAccessor;
import net.earthcomputer.multiconnect.protocols.v1_12.Protocol_1_12;
import net.earthcomputer.multiconnect.protocols.v1_12.RecipeInfo;
import net.earthcomputer.multiconnect.protocols.v1_12.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_13.Protocol_1_13_2;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class Protocol_1_11_2 extends Protocol_1_12 {
    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        if (clazz == Player.class && (data == PlayerAccessor.getDataShoulderLeft() || data == PlayerAccessor.getDataShoulderRight())) {
            return false;
        }
        if (clazz == AbstractIllager.class && data == Protocol_1_13_2.OLD_ILLAGER_FLAGS) {
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
    public void registerCommands(CommandDispatcher<SharedSuggestionProvider> dispatcher, @Nullable Set<String> serverCommands) {
        super.registerCommands(dispatcher, serverCommands);

        BrigadierRemover.of(dispatcher).get("advancement").remove();
        BrigadierRemover.of(dispatcher).get("function").remove();
        BrigadierRemover.of(dispatcher).get("recipe").remove();
        BrigadierRemover.of(dispatcher).get("reload").remove();

        Commands_1_12_2.registerVanilla(dispatcher, serverCommands, "achievement", AchievementCommand::register);
    }
}
