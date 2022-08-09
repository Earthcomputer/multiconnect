package net.earthcomputer.multiconnect.protocols.v1_12;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_13.Protocol_1_13;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Protocol_1_12_2 extends Protocol_1_13 {

    @Override
    public void setup() {
        TabCompletionManager.reset();
        super.setup();
    }

    @Override
    public float getBlockDestroySpeed(BlockState state, float destroySpeed) {
        if (state.getBlock() instanceof InfestedBlock) {
            return 0.75f;
        }
        return super.getBlockDestroySpeed(state, destroySpeed);
    }

    @Override
    public BlockState getActualState(Level world, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof FlowerPotBlock) {
            if (world.getBlockEntity(pos) instanceof FlowerPotBlockEntity flowerPot) {
                BlockState flowerPotState = flowerPot.getFlowerPotState();
                if (flowerPotState != null) {
                    return flowerPotState;
                }
            }
        } else if (state.getBlock() instanceof AbstractSkullBlock) {
            if (world.getBlockEntity(pos) instanceof ISkullBlockEntity skull) {
                return skull.multiconnect_getActualState();
            }
        }
        return super.getActualState(world, pos, state);
    }

    public List<RecipeInfo<?>> getRecipes() {
        return Recipes_1_12_2.getRecipes();
    }

    public void registerCommands(CommandDispatcher<SharedSuggestionProvider> dispatcher, @Nullable Set<String> serverCommands) {
        Commands_1_12_2.register(dispatcher, serverCommands);
    }

    @Override
    public boolean shouldBlockChangeReplaceBlockEntity(Block oldBlock, Block newBlock) {
        if (!super.shouldBlockChangeReplaceBlockEntity(oldBlock, newBlock))
            return false;

        if (oldBlock instanceof AbstractSkullBlock && newBlock instanceof AbstractSkullBlock)
            return false;
        if (oldBlock instanceof AbstractBannerBlock && newBlock instanceof AbstractBannerBlock)
            return false;
        if (oldBlock instanceof FlowerPotBlock && newBlock instanceof FlowerPotBlock)
            return false;

        return true;
    }
}
