package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.BlockStateArgumentType_1_12_2.*;
import static net.minecraft.command.argument.BlockPosArgumentType.*;
import static net.minecraft.command.argument.NbtCompoundTagArgumentType.*;

public class TestForBlockCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("testforblock")
            .then(argument("pos", blockPos())
                .then(argument("block", testBlockState())
                    .executes(ctx -> 0)
                    .then(argument("nbt", nbtCompound())
                        .executes(ctx -> 0)))));
    }

}
