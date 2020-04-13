package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.ISuggestionProvider;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.minecraft.command.arguments.BlockPosArgument.blockPos;
import static net.minecraft.command.arguments.NBTCompoundTagArgument.nbt;

public class BlockDataCommand {

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher) {
        dispatcher.register(literal("blockdata")
            .then(argument("pos", blockPos())
                .then(argument("nbt", nbt())
                    .executes(ctx -> 0))));
    }

}
