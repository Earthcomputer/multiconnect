package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandSource;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.command.argument.TextArgumentType.*;

public class TitleCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("title")
            .then(argument("player", players())
                .then(literal("title")
                    .then(argument("value", text())
                        .executes(ctx -> 0)))
                .then(literal("subtitle")
                    .then(argument("value", text())
                        .executes(ctx -> 0)))
                .then(literal("actionbar")
                    .then(argument("value", text())
                        .executes(ctx -> 0)))
                .then(literal("clear")
                    .executes(ctx -> 0))
                .then(literal("reset")
                    .executes(ctx -> 0))
                .then(literal("times")
                    .then(argument("fadeIn", integer())
                        .then(argument("stay", integer())
                            .then(argument("fadeOut", integer())
                                .executes(ctx -> 0)))))));
    }

}
