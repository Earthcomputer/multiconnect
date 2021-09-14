package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

@Message
public class SPacketAdvancementUpdate {
    public boolean clearCurrent;
    public List<Task> toAdd;
    public List<Identifier> toRemove;
    public List<Progress> toSetProgress;

    @Message
    public static class Task {
        public Identifier id;
        public Optional<Identifier> parentId;
        public Optional<Display> display;
        public List<Identifier> criteria;
        public List<List<String>> requirements;

        @Message
        public static class Display {
            public CommonTypes.Text title;
            public CommonTypes.Text description;
            public CommonTypes.ItemStack icon;
            public FrameType frameType;
            @Type(Types.INT)
            public int flags;
            @OnlyIf("hasBackgroundTexture")
            public Identifier backgroundTexture;
            public float x;
            public float y;

            public static boolean hasBackgroundTexture(@Argument("flags") int flags) {
                return (flags & 1) != 0;
            }

            public enum FrameType {
                TASK, CHALLENGE, GOAL
            }
        }
    }

    @Message
    public static class Progress {
        public List<Entry> entries;

        @Message
        public static class Entry {
            public Identifier criterion;
            public OptionalLong time;
        }
    }
}
