package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.CommandSource;
import net.minecraft.util.Identifier;

import java.util.Collection;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.command.arguments.IdentifierArgumentType.*;

public class AdvancementCommand {

    private static final SuggestionProvider<CommandSource> ADVANCEMENT_SUGGESTOR = (ctx, builder) -> {
        assert MinecraftClient.getInstance().getNetworkHandler() != null;
        Collection<Advancement> advancements = MinecraftClient.getInstance().getNetworkHandler().getAdvancementHandler().getManager().getAdvancements();
        return CommandSource.suggestIdentifiers(advancements.stream().map(Advancement::getId), builder);
    };
    private static final SuggestionProvider<CommandSource> CRITERIA_SUGGESTOR = (ctx, builder) -> {
        assert MinecraftClient.getInstance().getNetworkHandler() != null;
        Identifier id = ctx.getArgument("advancement", Identifier.class);
        Advancement advancement = MinecraftClient.getInstance().getNetworkHandler().getAdvancementHandler().getManager().get(id);
        if (advancement == null)
            return builder.buildFuture();
        return CommandSource.suggestMatching(advancement.getCriteria().keySet(), builder);
    };

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("advancement")
            .then(literal("grant")
                .then(tail()))
            .then(literal("revoke")
                .then(tail()))
            .then(literal("test")
                .then(argument("player", players())
                    .then(argument("advancement", identifier())
                        .suggests(ADVANCEMENT_SUGGESTOR)
                        .executes(ctx -> 0)
                        .then(argument("criteria", word())
                            .suggests(CRITERIA_SUGGESTOR)
                            .executes(ctx -> 0))))));
    }

    private static ArgumentBuilder<CommandSource, ?> tail() {
        return argument("player", players())
                .then(literal("only")
                    .then(argument("advancement", identifier())
                        .suggests(ADVANCEMENT_SUGGESTOR)
                        .executes(ctx -> 0)
                        .then(argument("criteria", word())
                            .suggests(CRITERIA_SUGGESTOR)
                            .executes(ctx -> 0))))
                .then(literal("until")
                    .then(argument("advancement", identifier())
                        .suggests(ADVANCEMENT_SUGGESTOR)
                        .executes(ctx -> 0)))
                .then(literal("from")
                    .then(argument("advancement", identifier())
                        .suggests(ADVANCEMENT_SUGGESTOR)
                        .executes(ctx -> 0)))
                .then(literal("through")
                    .then(argument("advancement", identifier())
                        .suggests(ADVANCEMENT_SUGGESTOR)
                        .executes(ctx -> 0)))
                .then(literal("everything")
                    .executes(ctx -> 0));
    }

}
