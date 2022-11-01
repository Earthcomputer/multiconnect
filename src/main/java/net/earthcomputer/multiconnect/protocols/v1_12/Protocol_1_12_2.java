package net.earthcomputer.multiconnect.protocols.v1_12;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_13.Protocol_1_13;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class Protocol_1_12_2 extends Protocol_1_13 {
    @Override
    public float getBlockDestroySpeed(BlockState state, float destroySpeed) {
        if (state.getBlock() instanceof InfestedBlock) {
            return 0.75f;
        }
        return super.getBlockDestroySpeed(state, destroySpeed);
    }

    public void registerCommands(CommandDispatcher<SharedSuggestionProvider> dispatcher, @Nullable Set<String> serverCommands) {
        Commands_1_12_2.register(dispatcher, serverCommands);
    }
}
