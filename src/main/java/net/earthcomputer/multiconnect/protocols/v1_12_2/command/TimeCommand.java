package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandSource;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.EnumArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.UnionArgumentType.*;

public class TimeCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
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
