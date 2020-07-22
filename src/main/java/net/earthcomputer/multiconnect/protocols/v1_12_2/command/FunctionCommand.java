package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.CommandSource;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.command.argument.IdentifierArgumentType.*;

public class FunctionCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("function")
            .then(argument("name", identifier())
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
