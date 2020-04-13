package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.command.arguments.ResourceLocationArgument.resourceLocation;

public class AdvancementCommand {

    private static final SuggestionProvider<ISuggestionProvider> ADVANCEMENT_SUGGESTOR = (ctx, builder) -> {
        assert Minecraft.getInstance().getConnection() != null;
        Collection<Advancement> advancements = Minecraft.getInstance().getConnection().getAdvancementManager().getAdvancementList().getAll();
        return ISuggestionProvider.func_212476_a(advancements.stream().map(Advancement::getId), builder);
    };
    private static final SuggestionProvider<ISuggestionProvider> CRITERIA_SUGGESTOR = (ctx, builder) -> {
        assert Minecraft.getInstance().getConnection() != null;
        ResourceLocation id = ctx.getArgument("advancement", ResourceLocation.class);
        Advancement advancement = Minecraft.getInstance().getConnection().getAdvancementManager().getAdvancementList().getAdvancement(id);
        if (advancement == null)
            return builder.buildFuture();
        return ISuggestionProvider.suggest(advancement.getCriteria().keySet(), builder);
    };

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher) {
        dispatcher.register(literal("advancement")
            .then(literal("grant")
                .then(tail()))
            .then(literal("revoke")
                .then(tail()))
            .then(literal("test")
                .then(argument("player", players())
                    .then(argument("advancement", resourceLocation())
                        .suggests(ADVANCEMENT_SUGGESTOR)
                        .executes(ctx -> 0)
                        .then(argument("criteria", word())
                            .suggests(CRITERIA_SUGGESTOR)
                            .executes(ctx -> 0))))));
    }

    private static ArgumentBuilder<ISuggestionProvider, ?> tail() {
        return argument("player", players())
                .then(literal("only")
                    .then(argument("advancement", resourceLocation())
                        .suggests(ADVANCEMENT_SUGGESTOR)
                        .executes(ctx -> 0)
                        .then(argument("criteria", word())
                            .suggests(CRITERIA_SUGGESTOR)
                            .executes(ctx -> 0))))
                .then(literal("until")
                    .then(argument("advancement", resourceLocation())
                        .suggests(ADVANCEMENT_SUGGESTOR)
                        .executes(ctx -> 0)))
                .then(literal("from")
                    .then(argument("advancement", resourceLocation())
                        .suggests(ADVANCEMENT_SUGGESTOR)
                        .executes(ctx -> 0)))
                .then(literal("through")
                    .then(argument("advancement", resourceLocation())
                        .suggests(ADVANCEMENT_SUGGESTOR)
                        .executes(ctx -> 0)))
                .then(literal("everything")
                    .executes(ctx -> 0));
    }

}
