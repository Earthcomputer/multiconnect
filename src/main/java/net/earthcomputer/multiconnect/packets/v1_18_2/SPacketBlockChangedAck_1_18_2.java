package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketBlockChangedAck;
import net.earthcomputer.multiconnect.packets.latest.SPacketBlockChangedAck_Latest;
import net.earthcomputer.multiconnect.protocols.v1_18.DiggingTracker;

import java.util.ArrayList;
import java.util.List;

@MessageVariant(minVersion = Protocols.V1_14_4, maxVersion = Protocols.V1_18_2)
public class SPacketBlockChangedAck_1_18_2 implements SPacketBlockChangedAck {
    public CommonTypes.BlockPos pos;
    @Registry(Registries.BLOCK_STATE)
    public int block;
    public Action action;
    public boolean successful;

    @Handler
    public static List<SPacketBlockChangedAck_Latest> handle(
            @Argument("pos") CommonTypes.BlockPos pos_,
            @Argument("action") Action action,
            @GlobalData DiggingTracker diggingTracker
    ) {
        List<SPacketBlockChangedAck_Latest> packets = new ArrayList<>(1);

        var pos = (CommonTypes.BlockPos_Latest) pos_;
        Integer sequence = diggingTracker.pos2SequenceId().remove(new DiggingTracker.DiggingPos(action, pos.packedData));
        if (sequence != null) {
            int maxSequence = diggingTracker.maxSequenceId().accumulateAndGet(sequence, Math::max);
            if (sequence == maxSequence) {
                var sequencePacket = new SPacketBlockChangedAck_Latest();
                sequencePacket.sequence = sequence;
                packets.add(sequencePacket);
            }
        }

        return packets;
    }

    @NetworkEnum
    public enum Action {
        STARTED_DIGGING, CANCELED_DIGGING, FINISHED_DIGGING
    }
}
