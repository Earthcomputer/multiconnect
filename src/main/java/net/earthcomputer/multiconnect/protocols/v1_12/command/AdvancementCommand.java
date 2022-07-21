package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.commands.arguments.ResourceLocationArgument.*;

public class AdvancementCommand {

    private static final SuggestionProvider<SharedSuggestionProvider> ADVANCEMENT_SUGGESTOR = (ctx, builder) -> {
        assert Minecraft.getInstance().getConnection() != null;
        Collection<Advancement> advancements = Minecraft.getInstance().getConnection().getAdvancements().getAdvancements().getAllAdvancements();
        return SharedSuggestionProvider.suggestResource(advancements.stream().map(Advancement::getId), builder);
    };
    private static final SuggestionProvider<SharedSuggestionProvider> CRITERIA_SUGGESTOR = (ctx, builder) -> {
        assert Minecraft.getInstance().getConnection() != null;
        ResourceLocation id = ctx.getArgument("advancement", ResourceLocation.class);
        Advancement advancement = Minecraft.getInstance().getConnection().getAdvancements().getAdvancements().get(id);
        if (advancement == null)
            return builder.buildFuture();
        return SharedSuggestionProvider.suggest(advancement.getCriteria().keySet(), builder);
    };

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("advancement")
            .then(literal("grant")
                .then(tail()))
            .then(literal("revoke")
                .then(tail()))
            .then(literal("test")
                .then(argument("player", players())
                    .then(argument("advancement", id())
                        .suggests(ADVANCEMENT_SUGGESTOR)
                        .executes(ctx -> 0)
                        .then(argument("criteria", word())
                            .suggests(CRITERIA_SUGGESTOR)
                            .executes(ctx -> 0))))));
    }

    private static ArgumentBuilder<SharedSuggestionProvider, ?> tail() {
        return argument("player", players())
                .then(literal("only")
                    .then(argument("advancement", id())
                        .suggests(ADVANCEMENT_SUGGESTOR)
                        .executes(ctx -> 0)
                        .then(argument("criteria", word())
                            .suggests(CRITERIA_SUGGESTOR)
                            .executes(ctx -> 0))))
                .then(literal("until")
                    .then(argument("advancement", id())
                        .suggests(ADVANCEMENT_SUGGESTOR)
                        .executes(ctx -> 0)))
                .then(literal("from")
                    .then(argument("advancement", id())
                        .suggests(ADVANCEMENT_SUGGESTOR)
                        .executes(ctx -> 0)))
                .then(literal("through")
                    .then(argument("advancement", id())
                        .suggests(ADVANCEMENT_SUGGESTOR)
                        .executes(ctx -> 0)))
                .then(literal("everything")
                    .executes(ctx -> 0));
    }

}
