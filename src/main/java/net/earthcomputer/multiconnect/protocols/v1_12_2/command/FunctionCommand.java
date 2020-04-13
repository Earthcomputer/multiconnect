package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.SuggestionProviders;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.command.arguments.ResourceLocationArgument.resourceLocation;

public class FunctionCommand {

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher) {
        dispatcher.register(literal("function")
            .then(argument("name", resourceLocation())
                .suggests(SuggestionProviders.ASK_SERVER)
                .executes(ctx -> 0)
                .then(literal("if")
                    .then(argument("selector", entities())
                        .executes(ctx -> 0)))
                .then(literal("unless")
                    .then(argument("selector", entities())
                        .executes(ctx -> 0)))));
    }

}
