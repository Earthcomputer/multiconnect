package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;

import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.*;

public class SetWorldSpawnCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("setworldspawn")
            .executes(ctx -> 0)
            .then(argument("pos", blockPos())
                .executes(ctx -> 0)));
    }

}
