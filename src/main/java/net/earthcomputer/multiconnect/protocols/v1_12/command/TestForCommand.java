package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;

import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.commands.arguments.CompoundTagArgument.*;

public class TestForCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("testfor")
            .then(argument("entity", entities())
                .executes(ctx -> 0)
                .then(argument("nbt", compoundTag())
                    .executes(ctx -> 0))));
    }

}
