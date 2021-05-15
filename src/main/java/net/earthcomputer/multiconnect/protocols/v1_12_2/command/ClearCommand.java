package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.ItemArgumentType_1_12_2.*;
import static net.minecraft.command.argument.NbtCompoundArgumentType.*;

public class ClearCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
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
                            .then(argument("nbt", nbtCompound())
                                .executes(ctx -> 0)))))));
    }

}
