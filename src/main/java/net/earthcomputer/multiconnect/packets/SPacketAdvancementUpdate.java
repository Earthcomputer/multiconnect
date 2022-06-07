package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

@MessageVariant
public class SPacketAdvancementUpdate {
    public boolean clearCurrent;
    public List<Task> toAdd;
    public List<Identifier> toRemove;
    public List<Progress> toSetProgress;

    @MessageVariant
    public static class Task {
        public Identifier id;
        public Optional<Identifier> parentId;
        public Optional<Display> display;
        public List<Identifier> criteria;
        public List<List<String>> requirements;

        @MessageVariant
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

            @NetworkEnum
            public enum FrameType {
                TASK, CHALLENGE, GOAL
            }
        }
    }

    @MessageVariant
    public static class Progress {
        public Identifier id;
        public List<Entry> entries;

        @MessageVariant
        public static class Entry {
            public String criterion;
            @Type(Types.LONG)
            public OptionalLong time;
        }
    }
}
