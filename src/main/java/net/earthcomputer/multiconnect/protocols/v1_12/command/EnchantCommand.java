package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.Registries;

import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.commands.arguments.ResourceArgument.*;

public class EnchantCommand {

    public static void register(CommandBuildContext context, CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("enchant")
            .then(argument("target", entities())
                .then(argument("enchantment", resource(context, Registries.ENCHANTMENT))
                    .executes(ctx -> 0))));
    }

}
