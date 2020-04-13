package net.earthcomputer.multiconnect.protocols.v1_12_2;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.Suggestion;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.network.play.client.CTabCompletePacket;
import net.minecraft.network.play.server.SCommandListPacket;
import net.minecraft.network.play.server.STabCompletePacket;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TabCompletionManager {

    private static final Queue<Entry> entries = new ArrayDeque<>();
    private static final Queue<CompletableFuture<List<String>>> customCompletions = new ArrayDeque<>();

    public static void reset() {
        entries.clear();
        customCompletions.clear();
    }

    public static void addTabCompletionRequest(int id, String message) {
        synchronized (entries) {
            entries.add(new Entry(id, message));
        }
    }

    public static Entry nextEntry() {
        synchronized (entries) {
            return entries.poll();
        }
    }

    public static void requestCommandList() {
        assert Minecraft.getInstance().getConnection() != null;
        Minecraft.getInstance().getConnection().sendPacket(new CTabCompletePacket(-1, "/"));
    }

    public static CompletableFuture<List<String>> requestCustomCompletion(String command) {
        assert Minecraft.getInstance().getConnection() != null;
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        customCompletions.add(future);
        Minecraft.getInstance().getConnection().sendPacket(new CTabCompletePacket(-2, command));
        return future;
    }

    public static boolean handleCustomCompletions(STabCompletePacket packet) {
        if (packet.getTransactionId() == -1) {
            CommandDispatcher<ISuggestionProvider> dispatcher = new CommandDispatcher<>();
            Commands_1_12_2.register(dispatcher, packet.getSuggestions().getList().stream()
                    .map(Suggestion::getText)
                    .filter(str -> !str.isEmpty())
                    .map(str -> str.substring(1))
                    .collect(Collectors.toSet()));
            assert Minecraft.getInstance().getConnection() != null;
            Minecraft.getInstance().getConnection().handleCommandList(new SCommandListPacket(dispatcher.getRoot()));
            return true;
        } else if (packet.getTransactionId() == -2) {
            if (customCompletions.isEmpty())
                return false;
            customCompletions.remove().complete(packet.getSuggestions().getList().stream().map(Suggestion::getText).collect(Collectors.toList()));
            return true;
        } else {
            return false;
        }
    }

    public static final class Entry {
        private final int id;
        private final String message;

        public Entry(int id, String message) {
            this.id = id;
            this.message = message;
        }

        public int getId() {
            return id;
        }

        public String getMessage() {
            return message;
        }
    }

}
