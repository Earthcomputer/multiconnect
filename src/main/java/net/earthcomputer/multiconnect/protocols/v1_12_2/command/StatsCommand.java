package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EnumArgumentType.*;
import static net.minecraft.command.argument.BlockPosArgumentType.*;

public class StatsCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("stats")
            .then(literal("block")
                .then(argument("pos", blockPos())
                    .then(set())
                    .then(clear())))
            .then(literal("entity")
                .then(argument("target", entities())
                    .then(set())
                    .then(clear()))));
    }

    private static ArgumentBuilder<CommandSource, ?> set() {
        return literal("set")
                .then(argument("stat", statsType())
                    .then(argument("selector", entities())
                        .then(argument("objective", word())
                            .suggests(SuggestionProviders.ASK_SERVER)
                            .executes(ctx -> 0))));
    }

    private static ArgumentBuilder<CommandSource, ?> clear() {
        return literal("clear")
                .then(argument("stat", statsType())
                    .executes(ctx -> 0));
    }

    private static ArgumentType<?> statsType() {
        return enumArg("SuccessCount", "AffectedBlocks", "AffectedEntities", "AffectedItems", "QueryResult");
    }

}
