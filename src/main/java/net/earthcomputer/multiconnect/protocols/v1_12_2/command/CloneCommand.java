package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandSource;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EnumArgumentType.*;
import static net.minecraft.command.argument.BlockPosArgumentType.*;

public class CloneCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("clone")
            .then(argument("pos1", blockPos())
                .then(argument("pos2", blockPos())
                    .then(argument("pos", blockPos())
                        .executes(ctx -> 0)
                        .then(argument("maskMode", enumArg("replace", "masked", "filtered"))
                            .executes(ctx -> 0)
                            .then(argument("cloneMode", enumArg("normal", "force", "move"))
                                .executes(ctx -> 0)))))));
    }

}
