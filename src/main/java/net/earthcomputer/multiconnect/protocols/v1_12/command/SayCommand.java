package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;

public class SayCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher, String name) {
        dispatcher.register(literal(name)
            .then(argument("message", greedyString())
                .executes(ctx -> 0)));
    }

}
