package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.ISuggestionProvider;

import static com.mojang.brigadier.arguments.BoolArgumentType.*;
import static com.mojang.brigadier.arguments.DoubleArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.command.arguments.Vec2Argument.vec2;

public class SpreadPlayersCommand {

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher) {
        CommandNode<ISuggestionProvider> respectTeams = argument("respectTeams", bool())
                .executes(ctx -> 0)
                .build();
        CommandNode<ISuggestionProvider> target = argument("target", entities())
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
