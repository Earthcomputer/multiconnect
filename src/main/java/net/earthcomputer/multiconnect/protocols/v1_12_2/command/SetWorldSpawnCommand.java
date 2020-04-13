package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.ISuggestionProvider;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.minecraft.command.arguments.BlockPosArgument.blockPos;

public class SetWorldSpawnCommand {

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher) {
        dispatcher.register(literal("setworldspawn")
            .executes(ctx -> 0)
            .then(argument("pos", blockPos())
                .executes(ctx -> 0)));
    }

}
