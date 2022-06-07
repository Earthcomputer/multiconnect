package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketStatistics;

import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_12_2)
public class SPacketStatistics_1_12_2 implements SPacketStatistics {
    public List<StatWithValue> statistics;

    @MessageVariant
    public static class StatWithValue {
        public String stat;
        public int value;
    }
}
