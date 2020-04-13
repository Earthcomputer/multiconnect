package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.ISuggestionProvider;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.BlockStateArgumentType_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EnumArgumentType.*;
import static net.minecraft.command.arguments.BlockPosArgument.blockPos;
import static net.minecraft.command.arguments.NBTCompoundTagArgument.nbt;

public class SetblockCommand {

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher) {
        dispatcher.register(literal("setblock")
            .then(argument("pos", blockPos())
                .then(argument("block", blockState())
                    .executes(ctx -> 0)
                    .then(argument("oldBlockHandling", enumArg("replace", "destroy", "keep"))
                        .executes(ctx -> 0)
                        .then(argument("nbt", nbt())
                            .executes(ctx -> 0))))));
    }

}
