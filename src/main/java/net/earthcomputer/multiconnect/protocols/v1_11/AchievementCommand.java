package net.earthcomputer.multiconnect.protocols.v1_11;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;

import static net.earthcomputer.multiconnect.protocols.v1_11.AchievementArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EntityArgumentType_1_12_2.*;

public class AchievementCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("achievement")
            .then(literal("give")
                .then(literal("*")
                    .executes(ctx -> 0)
                    .then(argument("player", players())
                        .executes(ctx -> 0)))
                .then(argument("achievement", achievement())
                    .executes(ctx -> 0)
                    .then(argument("player", players())
                        .executes(ctx -> 0))))
            .then(literal("take")
                .then(literal("*")
                    .executes(ctx -> 0)
                    .then(argument("player", players())
                        .executes(ctx -> 0)))
                .then(argument("achievement", achievement())
                    .executes(ctx -> 0)
                    .then(argument("player", players())
                        .executes(ctx -> 0)))));
    }

}
