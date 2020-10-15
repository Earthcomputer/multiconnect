package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;

public class WeatherCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("weather")
            .then(literal("clear")
                .executes(ctx -> 0)
                .then(argument("time", integer(1, 1000000))
                    .executes(ctx -> 0)))
            .then(literal("rain")
                .executes(ctx -> 0)
                .then(argument("time", integer(1, 1000000))
                    .executes(ctx -> 0)))
            .then(literal("thunder")
                .executes(ctx -> 0)
                .then(argument("time", integer(1, 1000000)))));
    }

}
