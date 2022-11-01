package net.earthcomputer.multiconnect.protocols.v1_11;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.v1_12.Protocol_1_12;
import net.earthcomputer.multiconnect.protocols.v1_12.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class Protocol_1_11_2 extends Protocol_1_12 {
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
