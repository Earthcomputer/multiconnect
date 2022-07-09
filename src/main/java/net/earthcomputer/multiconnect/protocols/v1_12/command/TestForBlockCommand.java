package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;

import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.BlockStateArgumentType_1_12_2.*;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.*;
import static net.minecraft.commands.arguments.CompoundTagArgument.*;

public class TestForBlockCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("testforblock")
            .then(argument("pos", blockPos())
                .then(argument("block", testBlockState())
                    .executes(ctx -> 0)
                    .then(argument("nbt", compoundTag())
                        .executes(ctx -> 0)))));
    }

}
