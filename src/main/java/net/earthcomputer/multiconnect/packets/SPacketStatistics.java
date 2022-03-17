package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;

import java.util.List;

@MessageVariant
public class SPacketStatistics {
    public List<Statistic> statistics;

    @Polymorphic
    @MessageVariant
    public static abstract class Statistic {
        @Registry(Registries.STAT_TYPE)
        public int category;
    }

    @Polymorphic(stringValue = "mined")
    @MessageVariant
    public static class BlockStatistic extends Statistic {
        @Registry(Registries.BLOCK)
        public int block;
    }

    @Polymorphic(stringValue = {"crafted", "used", "broken", "picked_up", "dropped"})
    @MessageVariant
    public static class ItemStatistic extends Statistic {
        @Registry(Registries.ITEM)
        public int item;
    }

    @Polymorphic(stringValue = {"killed", "killed_by"})
    @MessageVariant
    public static class EntityStatistic extends Statistic {
        @Registry(Registries.ENTITY_TYPE)
        public int entityType;
    }

    @Polymorphic(stringValue = "custom")
    @MessageVariant
    public static class CustomStatistic extends Statistic {
        @Registry(Registries.CUSTOM_STAT)
        public int statId;
    }
}
