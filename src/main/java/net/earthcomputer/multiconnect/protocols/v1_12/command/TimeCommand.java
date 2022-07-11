package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EnumArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.UnionArgumentType.*;

public class TimeCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("time")
            .then(literal("set")
                .then(argument("time", union(enumArg("day", "night"), integer(0)))
                    .executes(ctx -> 0)))
            .then(literal("add")
                .then(argument("time", integer(0))
                    .executes(ctx -> 0)))
            .then(literal("query")
                .then(literal("daytime")
                    .executes(ctx -> 0))
                .then(literal("day")
                    .executes(ctx -> 0))
                .then(literal("gametime")
                    .executes(ctx -> 0))));
    }

}
