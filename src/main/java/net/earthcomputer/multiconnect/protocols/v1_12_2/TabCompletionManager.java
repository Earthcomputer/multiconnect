package net.earthcomputer.multiconnect.protocols.v1_12_2;

import java.util.ArrayDeque;
import java.util.Queue;

public class TabCompletionManager {

    private static final Queue<Entry> entries = new ArrayDeque<>();

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
