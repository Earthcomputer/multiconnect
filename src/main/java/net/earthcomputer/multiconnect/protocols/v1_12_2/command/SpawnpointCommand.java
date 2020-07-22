package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandSource;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.command.argument.BlockPosArgumentType.*;

public class SpawnpointCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("spawnpoint")
            .executes(ctx -> 0)
            .then(argument("player", players())
                .executes(ctx -> 0)
                .then(argument("pos", blockPos())
                    .executes(ctx -> 0))));
    }

}
