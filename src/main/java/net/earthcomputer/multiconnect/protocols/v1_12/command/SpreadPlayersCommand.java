package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;

import static com.mojang.brigadier.arguments.BoolArgumentType.*;
import static com.mojang.brigadier.arguments.DoubleArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.commands.arguments.coordinates.Vec2Argument.*;

public class SpreadPlayersCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        var respectTeams = argument("respectTeams", bool())
                .executes(ctx -> 0)
                .build();
        var target = argument("target", entities())
                .executes(ctx -> 0)
                .redirect(respectTeams)
                .build();
        respectTeams.addChild(target);
        dispatcher.register(literal("spreadplayers")
                .then(argument("center", vec2())
                        .then(argument("spreadDistance", doubleArg(0))
                                .then(argument("maxRange", doubleArg(1))
                                        .then(respectTeams)))));
    }

}
