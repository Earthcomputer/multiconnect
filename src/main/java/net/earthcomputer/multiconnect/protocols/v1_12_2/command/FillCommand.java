package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.BlockStateArgumentType_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EnumArgumentType.*;
import static net.minecraft.command.argument.BlockPosArgumentType.*;
import static net.minecraft.command.argument.NbtCompoundTagArgumentType.*;

public class FillCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("fill")
            .then(argument("pos1", blockPos())
                .then(argument("pos2", blockPos())
                    .then(argument("block", blockState())
                        .executes(ctx -> 0)
                        .then(argument("oldBlockHandling", enumArg("replace", "destroy", "keep", "hollow", "outline"))
                            .executes(ctx -> 0)
                            .then(argument("nbt", nbtCompound())
                                .executes(ctx -> 0)))))));
    }

}
