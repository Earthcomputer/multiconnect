package net.earthcomputer.multiconnect.api;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class ProtocolBehavior {
    public void onSetup() {
    }

    public void onDisable() {
    }

    public Block[] getBlocksWithChangedCollision() {
        return new Block[0];
    }

    @Nullable
    public Float getDestroySpeed(BlockState state, float destroySpeed) {
        return null;
    }

    @Nullable
    public Float getExplosionResistance(Block block, float explosionResistance) {
        return null;
    }

    public void onCommandRegistration(CommandRegistrationArgs args) {
    }

    @ApiStatus.NonExtendable
    public interface CommandRegistrationArgs {
        CommandBuildContext context();

        CommandDispatcher<SharedSuggestionProvider> dispatcher();

        @Nullable
        Set<String> serverCommands();
    }
}
