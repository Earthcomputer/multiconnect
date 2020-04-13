package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.SuggestionProviders;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.command.arguments.ResourceLocationArgument.resourceLocation;

public class RecipeCommand {

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher) {
        dispatcher.register(literal("recipe")
            .then(literal("give")
                .then(argument("player", players())
                    .then(literal("*")
                        .executes(ctx -> 0))
                    .then(argument("recipe", resourceLocation())
                        .suggests(SuggestionProviders.ASK_SERVER)
                        .executes(ctx -> 0))))
            .then(literal("take")
                .then(argument("player", players())
                    .then(literal("*")
                        .executes(ctx -> 0))
                    .then(argument("recipe", resourceLocation())
                        .suggests(SuggestionProviders.ASK_SERVER)
                        .executes(ctx -> 0)))));
    }

}
