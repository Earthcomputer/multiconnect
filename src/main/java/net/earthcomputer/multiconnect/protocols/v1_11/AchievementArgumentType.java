package net.earthcomputer.multiconnect.protocols.v1_11;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class AchievementArgumentType implements ArgumentType<Advancement> {

    private static final Collection<String> EXAMPLES = Arrays.asList("achievement.openInventory", "achievement.mineWood");

    private static final DynamicCommandExceptionType NO_SUCH_ACHIEVEMENT_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("commands.achievement.unknownAchievement", arg));

    private AchievementArgumentType() {
    }

    public static AchievementArgumentType achievement() {
        return new AchievementArgumentType();
    }

    @Override
    public Advancement parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();

        String achievementName = reader.readUnquotedString();
        if (!achievementName.startsWith("achievement.")) {
            reader.setCursor(start);
            throw NO_SUCH_ACHIEVEMENT_EXCEPTION.createWithContext(reader, achievementName);
        }

        Advancement achievement = Achievements_1_11_2.ACHIEVEMENTS.get(achievementName.substring("achievement.".length()));
        if (achievement == null) {
            reader.setCursor(start);
            throw NO_SUCH_ACHIEVEMENT_EXCEPTION.createWithContext(reader, achievementName);
        }

        return achievement;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        SharedSuggestionProvider.suggest(Achievements_1_11_2.ACHIEVEMENTS.keySet().stream().map(it -> "achievement." + it), builder);
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
