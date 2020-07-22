package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandSource;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EnumArgumentType.*;
import static net.minecraft.command.argument.BlockPosArgumentType.*;

public class TestForBlocksCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("testforblocks")
            .then(argument("pos1", blockPos())
                .then(argument("pos2", blockPos())
                    .then(argument("pos", blockPos())
                        .executes(ctx -> 0)
                        .then(argument("mode", enumArg("masked", "all"))
                            .executes(ctx -> 0))))));
    }

}
