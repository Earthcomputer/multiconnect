package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.CommandSource;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.command.argument.IdentifierArgumentType.*;

public class RecipeCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("recipe")
            .then(literal("give")
                .then(argument("player", players())
                    .then(literal("*")
                        .executes(ctx -> 0))
                    .then(argument("recipe", identifier())
                        .suggests(SuggestionProviders.ASK_SERVER)
                        .executes(ctx -> 0))))
            .then(literal("take")
                .then(argument("player", players())
                    .then(literal("*")
                        .executes(ctx -> 0))
                    .then(argument("recipe", identifier())
                        .suggests(SuggestionProviders.ASK_SERVER)
                        .executes(ctx -> 0)))));
    }

}
