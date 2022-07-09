package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;

import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.BlockStateArgumentType_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EnumArgumentType.*;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.*;
import static net.minecraft.commands.arguments.CompoundTagArgument.*;

public class FillCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("fill")
            .then(argument("pos1", blockPos())
                .then(argument("pos2", blockPos())
                    .then(argument("block", blockState())
                        .executes(ctx -> 0)
                        .then(argument("oldBlockHandling", enumArg("replace", "destroy", "keep", "hollow", "outline"))
                            .executes(ctx -> 0)
                            .then(argument("nbt", compoundTag())
                                .executes(ctx -> 0)))))));
    }

}
