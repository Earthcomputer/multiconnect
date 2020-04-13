package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.ISuggestionProvider;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.command.arguments.BlockPosArgument.blockPos;

public class SpawnpointCommand {

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher) {
        dispatcher.register(literal("spawnpoint")
            .executes(ctx -> 0)
            .then(argument("player", players())
                .executes(ctx -> 0)
                .then(argument("pos", blockPos())
                    .executes(ctx -> 0))));
    }

}
