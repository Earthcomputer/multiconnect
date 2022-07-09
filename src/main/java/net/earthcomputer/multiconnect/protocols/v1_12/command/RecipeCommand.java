package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;

import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.commands.arguments.ResourceLocationArgument.*;

public class RecipeCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("recipe")
            .then(literal("give")
                .then(argument("player", players())
                    .then(literal("*")
                        .executes(ctx -> 0))
                    .then(argument("recipe", id())
                        .suggests(SuggestionProviders.ASK_SERVER)
                        .executes(ctx -> 0))))
            .then(literal("take")
                .then(argument("player", players())
                    .then(literal("*")
                        .executes(ctx -> 0))
                    .then(argument("recipe", id())
                        .suggests(SuggestionProviders.ASK_SERVER)
                        .executes(ctx -> 0)))));
    }

}
