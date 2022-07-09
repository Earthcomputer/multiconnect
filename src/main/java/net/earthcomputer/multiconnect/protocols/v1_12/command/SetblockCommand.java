package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;

import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.BlockStateArgumentType_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EnumArgumentType.*;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.*;
import static net.minecraft.commands.arguments.CompoundTagArgument.*;

public class SetblockCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("setblock")
            .then(argument("pos", blockPos())
                .then(argument("block", blockState())
                    .executes(ctx -> 0)
                    .then(argument("oldBlockHandling", enumArg("replace", "destroy", "keep"))
                        .executes(ctx -> 0)
                        .then(argument("nbt", compoundTag())
                            .executes(ctx -> 0))))));
    }

}
