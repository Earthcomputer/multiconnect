package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.ISuggestionProvider;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.BlockStateArgumentType_1_12_2.*;
import static net.minecraft.command.arguments.BlockPosArgument.blockPos;
import static net.minecraft.command.arguments.NBTCompoundTagArgument.nbt;

public class TestForBlockCommand {

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher) {
        dispatcher.register(literal("testforblock")
            .then(argument("pos", blockPos())
                .then(argument("block", testBlockState())
                    .executes(ctx -> 0)
                    .then(argument("nbt", nbt())
                        .executes(ctx -> 0)))));
    }

}
