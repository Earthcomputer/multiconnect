package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.SharedSuggestionProvider;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EntityArgumentType_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.ItemArgumentType_1_12_2.*;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.*;
import static net.minecraft.commands.arguments.SlotArgument.*;
import static net.minecraft.commands.arguments.CompoundTagArgument.*;

public class ReplaceItemCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("replaceitem")
            .then(literal("block")
                .then(argument("pos", blockPos())
                    .then(tail())))
            .then(literal("entity")
                .then(argument("target", entities())
                    .then(tail()))));
    }

    private static ArgumentBuilder<SharedSuggestionProvider, ?> tail() {
        return argument("slot", slot())
                .then(argument("item", item())
                    .executes(ctx -> 0)
                    .then(argument("count", integer(1, 64))
                        .executes(ctx -> 0)
                        .then(argument("damage", integer(0, Short.MAX_VALUE))
                            .executes(ctx -> 0)
                            .then(argument("nbt", compoundTag())
                                .executes(ctx -> 0)))));
    }

}
