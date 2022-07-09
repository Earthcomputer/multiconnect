package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;

public class TriggerCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("trigger")
            .then(argument("objective", word())
                .suggests(SuggestionProviders.ASK_SERVER)
                .then(literal("add")
                    .then(argument("value", integer())
                        .executes(ctx -> 0)))
                .then(literal("set")
                    .then(argument("value", integer())
                        .executes(ctx -> 0)))));
    }

}
