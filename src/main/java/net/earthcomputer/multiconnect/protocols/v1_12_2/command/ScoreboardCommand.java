package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.mojang.brigadier.arguments.BoolArgumentType.*;
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EnumArgumentType.*;
import static net.minecraft.command.arguments.ColorArgument.color;
import static net.minecraft.command.arguments.NBTCompoundTagArgument.nbt;
import static net.minecraft.command.arguments.ObjectiveCriteriaArgument.objectiveCriteria;

public class ScoreboardCommand {

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher) {
        dispatcher.register(literal("scoreboard")
            .then(literal("objectives")
                .then(literal("list")
                    .executes(ctx -> 0))
                .then(literal("add")
                    .then(argument("name", word())
                        .then(argument("criteria", objectiveCriteria())
                            .executes(ctx -> 0)
                            .then(argument("displayName", greedyString())
                                .executes(ctx -> 0)))))
                .then(literal("remove")
                    .then(argument("name", word())
                        .suggests(SuggestionProviders.ASK_SERVER)
                        .executes(ctx -> 0)))
                .then(literal("setdisplay")
                    .then(argument("slot", enumArg(displaySlots()).caseInsensitive())
                        .executes(ctx -> 0)
                        .then(argument("objective", word())
                            .suggests(SuggestionProviders.ASK_SERVER)
                            .executes(ctx -> 0)))))
            .then(literal("players")
                .then(literal("set")
                    .then(argument("player", entities())
                        .then(argument("objective", word())
                            .suggests(SuggestionProviders.ASK_SERVER)
                            .then(argument("score", integer())
                                .executes(ctx -> 0)
                                .then(argument("nbt", nbt())
                                    .executes(ctx -> 0))))))
                .then(literal("add")
                    .then(argument("player", entities())
                        .then(argument("objective", word())
                            .suggests(SuggestionProviders.ASK_SERVER)
                            .then(argument("count", integer(0))
                                .executes(ctx -> 0)
                                .then(argument("nbt", nbt())
                                    .executes(ctx -> 0))))))
                .then(literal("remove")
                    .then(argument("player", entities())
                        .then(argument("objective", word())
                            .suggests(SuggestionProviders.ASK_SERVER)
                            .then(argument("count", integer(0))
                                .executes(ctx -> 0)
                                .then(argument("nbt", nbt())
                                    .executes(ctx -> 0))))))
                .then(literal("reset")
                    .then(argument("player", entities())
                        .executes(ctx -> 0)
                        .then(argument("objective", word())
                            .suggests(SuggestionProviders.ASK_SERVER)
                            .executes(ctx -> 0))))
                .then(literal("list")
                    .executes(ctx -> 0)
                    .then(argument("name", entities())
                        .executes(ctx -> 0)))
                .then(literal("enable")
                    .then(argument("player", entities())
                        .then(argument("trigger", word())
                            .suggests(SuggestionProviders.ASK_SERVER)
                            .executes(ctx -> 0))))
                .then(literal("test")
                    .then(argument("player", entities())
                        .then(argument("objective", word())
                            .suggests(SuggestionProviders.ASK_SERVER)
                            .then(argument("min", integer())
                                .executes(ctx -> 0)
                                .then(argument("max", integer())
                                    .executes(ctx -> 0))))))
                .then(literal("operation")
                    .then(argument("targetName", entities())
                        .then(argument("targetObjective", word())
                            .suggests(SuggestionProviders.ASK_SERVER)
                            .then(argument("operation", enumArg("+=", "-=", "*=", "/=", "%=", "=", "<", ">", "><"))
                                .then(argument("selector", oneEntity())
                                    .then(argument("objective", word())
                                        .suggests(SuggestionProviders.ASK_SERVER)
                                        .executes(ctx -> 0)))))))
                .then(literal("tag")
                    .then(argument("player", entities())
                        .then(literal("add")
                            .then(argument("tagName", word())
                                .executes(ctx -> 0)
                                .then(argument("nbt", nbt())
                                    .executes(ctx -> 0))))
                        .then(literal("remove")
                            .then(argument("tagName", word())
                                .executes(ctx -> 0)
                                .then(argument("nbt", nbt())
                                    .executes(ctx -> 0))))
                        .then(literal("list")
                            .executes(ctx -> 0)))))
            .then(literal("teams")
                .then(literal("list")
                    .executes(ctx -> 0)
                    .then(argument("name", word())
                        .suggests(SuggestionProviders.ASK_SERVER)
                        .executes(ctx -> 0)))
                .then(literal("add")
                    .then(argument("name", word())
                        .executes(ctx -> 0)
                        .then(argument("displayName", greedyString())
                            .executes(ctx -> 0))))
                .then(literal("remove")
                    .then(argument("name", word())
                        .suggests(SuggestionProviders.ASK_SERVER)
                        .executes(ctx -> 0)))
                .then(literal("empty")
                    .then(argument("name", word())
                        .suggests(SuggestionProviders.ASK_SERVER)
                        .executes(ctx -> 0)))
                .then(literal("join")
                    .then(addPlayerList(argument("team", word())
                        .suggests(SuggestionProviders.ASK_SERVER))))
                .then(addPlayerList(literal("leave")))
                .then(literal("option")
                    .then(argument("team", word())
                        .suggests(SuggestionProviders.ASK_SERVER)
                        .then(literal("color")
                            .then(argument("value", color())
                                .executes(ctx -> 0)))
                        .then(literal("friendlyfire")
                            .then(argument("value", bool())
                                .executes(ctx -> 0)))
                        .then(literal("seeFriendlyInvisibles")
                            .then(argument("value", bool())
                                .executes(ctx -> 0)))
                        .then(literal("nametagVisibility")
                            .then(argument("value", enumArg("always", "never", "hideForOtherTeams", "hideForOwnTeam"))
                                .executes(ctx -> 0)))
                        .then(literal("deathMessageVisibility")
                            .then(argument("value", enumArg("always", "never", "hideForOtherTeams", "hideForOwnTeam"))
                                .executes(ctx -> 0)))
                        .then(literal("collisionRule")
                            .then(argument("value", enumArg("always", "never", "pushOtherTeams", "pushOwnTeam"))))))));
    }

    private static String[] displaySlots() {
        List<String> slots = new ArrayList<>();
        slots.add("list");
        slots.add("sidebar");
        slots.add("belowName");
        for (TextFormatting formatting : TextFormatting.values()) {
            if (formatting.isColor()) {
                slots.add("sidebar.team." + formatting.getFriendlyName().toLowerCase(Locale.ENGLISH));
            }
        }
        return slots.toArray(new String[0]);
    }

    private static CommandNode<ISuggestionProvider> addPlayerList(ArgumentBuilder<ISuggestionProvider, ?> parentBuilder) {
        CommandNode<ISuggestionProvider> parent = parentBuilder.executes(ctx -> 0).build();
        CommandNode<ISuggestionProvider> child = argument("player", entities())
                .executes(ctx -> 0)
                .redirect(parent)
                .build();
        parent.addChild(child);
        return parent;
    }

}
