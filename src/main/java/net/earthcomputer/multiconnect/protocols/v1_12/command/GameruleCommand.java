package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;

public class GameruleCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("gamerule")
            .then(argument("rule", word())
                .suggests(SuggestionProviders.ASK_SERVER)
                .executes(ctx -> 0)
                .then(argument("value", word())
                    .suggests(SuggestionProviders.ASK_SERVER)
                    .executes(ctx -> 0))));
    }

}
