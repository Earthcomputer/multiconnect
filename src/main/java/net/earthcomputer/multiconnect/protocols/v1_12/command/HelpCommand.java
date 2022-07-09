package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;

public class HelpCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher, String name) {
        dispatcher.register(literal(name)
            .executes(ctx -> 0)
            .then(argument("commandOrPage", word())
                .suggests(SuggestionProviders.ASK_SERVER)
                .executes(ctx -> 0)));
    }

}
