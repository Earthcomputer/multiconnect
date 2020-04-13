package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.ISuggestionProvider;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.command.arguments.NBTCompoundTagArgument.nbt;

public class TestForCommand {

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher) {
        dispatcher.register(literal("testfor")
            .then(argument("entity", entities())
                .executes(ctx -> 0)
                .then(argument("nbt", nbt())
                    .executes(ctx -> 0))));
    }

}
