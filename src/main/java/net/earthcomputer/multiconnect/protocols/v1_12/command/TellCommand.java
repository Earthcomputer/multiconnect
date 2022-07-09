package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EntityArgumentType_1_12_2.*;

public class TellCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher, String name) {
        dispatcher.register(literal(name)
            .then(argument("recipient", players())
                .then(argument("message", greedyString())
                    .executes(ctx -> 0))));
    }

}
