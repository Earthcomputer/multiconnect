package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.CommandSource;

import static com.mojang.brigadier.arguments.BoolArgumentType.*;
import static com.mojang.brigadier.arguments.DoubleArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.command.arguments.Vec2ArgumentType.*;

public class SpreadPlayersCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        CommandNode<CommandSource> respectTeams = argument("respectTeams", bool())
                .executes(ctx -> 0)
                .build();
        CommandNode<CommandSource> target = argument("target", entities())
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
