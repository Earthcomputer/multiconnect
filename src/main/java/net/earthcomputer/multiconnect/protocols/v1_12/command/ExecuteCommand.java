package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;

import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.BlockStateArgumentType_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.CommandArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.*;
import static net.minecraft.commands.arguments.coordinates.Vec3Argument.*;

public class ExecuteCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("execute")
            .then(argument("entity", entities())
                .then(argument("pos", vec3())
                    .then(literal("detect")
                        .then(argument("detectPos", blockPos())
                            .then(argument("block", testBlockState())
                                .then(argument("command", command(dispatcher))))))
                    .then(argument("command", command(dispatcher))))));
    }

}
