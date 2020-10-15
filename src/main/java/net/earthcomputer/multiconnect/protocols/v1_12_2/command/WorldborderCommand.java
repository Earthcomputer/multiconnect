package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.arguments.DoubleArgumentType.*;
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static com.mojang.brigadier.arguments.LongArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.minecraft.command.argument.Vec2ArgumentType.*;

public class WorldborderCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("worldborder")
            .then(literal("set")
                .then(argument("sizeInBlocks", doubleArg(1, 6E7))
                    .executes(ctx -> 0)
                    .then(argument("timeInSeconds", longArg(0, Long.MAX_VALUE / 1000))
                        .executes(ctx -> 0))))
            .then(literal("center")
                .then(argument("pos", vec2())
                    .executes(ctx -> 0)))
            .then(literal("damage")
                .then(literal("buffer")
                    .then(argument("sizeInBlocks", doubleArg(0))
                        .executes(ctx -> 0)))
                .then(literal("amount")
                    .then(argument("damagePerBlock", doubleArg(0))
                        .executes(ctx -> 0))))
            .then(literal("warning")
                .then(literal("time")
                    .then(argument("seconds", integer(0))
                        .executes(ctx -> 0)))
                .then(literal("distance")
                    .then(argument("warningDistance", integer(0)))))
            .then(literal("get")
                .executes(ctx -> 0))
            .then(literal("add")
                .then(argument("sizeInBlocks", doubleArg(-6E7, 6E7))
                    .executes(ctx -> 0)
                    .then(argument("timeInSeconds", longArg(0, Long.MAX_VALUE / 1000))
                        .executes(ctx -> 0)))));
    }

}
