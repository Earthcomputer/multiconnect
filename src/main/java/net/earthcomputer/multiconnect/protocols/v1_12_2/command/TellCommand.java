package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.ISuggestionProvider;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;

public class TellCommand {

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher, String name) {
        dispatcher.register(literal(name)
            .then(argument("recipient", players())
                .then(argument("message", greedyString())
                    .executes(ctx -> 0))));
    }

}
