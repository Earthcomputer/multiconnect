package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandSource;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.ItemArgumentType_1_12_2.*;
import static net.minecraft.command.arguments.NbtCompoundTagArgumentType.*;

public class GiveCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("give")
            .then(argument("player", players())
                .then(argument("item", item())
                    .executes(ctx -> 0)
                    .then(argument("count", integer(1, 64))
                        .executes(ctx -> 0)
                        .then(argument("damage", integer(0, Short.MAX_VALUE))
                            .executes(ctx -> 0)
                            .then(argument("nbt", nbtCompound())
                                .executes(ctx -> 0)))))));
    }

}
