package net.earthcomputer.multiconnect.protocols.v1_9;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.earthcomputer.multiconnect.protocols.v1_12.command.BrigadierRemover;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class Protocol_1_9_4 extends Protocol_1_10 {
    @Override
    public void registerCommands(CommandBuildContext context, CommandDispatcher<SharedSuggestionProvider> dispatcher, @Nullable Set<String> serverCommands) {
        super.registerCommands(context, dispatcher, serverCommands);
        BrigadierRemover.of(dispatcher).get("teleport").remove();
    }
}
