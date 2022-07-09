package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EntityArgumentType_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.ItemArgumentType_1_12_2.*;
import static net.minecraft.commands.arguments.CompoundTagArgument.*;

public class ClearCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("clear")
            .executes(ctx -> 0)
            .then(argument("player", players())
                .executes(ctx -> 0)
                .then(argument("item", item())
                    .executes(ctx -> 0)
                    .then(argument("damage", integer(-1))
                        .executes(ctx -> 0)
                        .then(argument("maxCount", integer(-1))
                            .executes(ctx -> 0)
                            .then(argument("nbt", compoundTag())
                                .executes(ctx -> 0)))))));
    }

}
