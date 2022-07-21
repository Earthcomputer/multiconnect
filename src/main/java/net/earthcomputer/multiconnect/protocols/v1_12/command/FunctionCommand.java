package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;

import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.commands.arguments.ResourceLocationArgument.*;

public class FunctionCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("function")
            .then(argument("name", id())
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
