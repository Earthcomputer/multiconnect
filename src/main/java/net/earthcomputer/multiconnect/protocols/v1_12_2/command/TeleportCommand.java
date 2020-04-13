package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.ISuggestionProvider;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.command.arguments.BlockPosArgument.blockPos;
import static net.minecraft.command.arguments.RotationArgument.rotation;

public class TeleportCommand {

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher) {
        dispatcher.register(literal("teleport")
            .then(argument("victim", entities())
                .then(argument("pos", blockPos())
                    .executes(ctx -> 0)
                    .then(argument("rot", rotation())
                        .executes(ctx -> 0)))));
    }

}
