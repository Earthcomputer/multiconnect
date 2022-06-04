package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketPlayerAction;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.v1_18_2.CPacketPlayerAction_1_18_2;
import net.earthcomputer.multiconnect.packets.v1_18_2.SPacketPlayerActionResponse_1_18_2;
import net.earthcomputer.multiconnect.protocols.v1_18_2.DiggingTracker;

@MessageVariant(minVersion = Protocols.V1_19)
public class CPacketPlayerAction_Latest implements CPacketPlayerAction {
    public Action action;
    public CommonTypes.BlockPos pos;
    @Type(Types.UNSIGNED_BYTE)
    public CommonTypes.Direction face;
    public int sequence;

    @Handler(protocol = Protocols.V1_18_2)
    public static CPacketPlayerAction_1_18_2 handle(
            @Argument(value = "this", translate = true) CPacketPlayerAction_1_18_2 translatedThis,
            @Argument("sequence") int sequence,
            @GlobalData DiggingTracker diggingTracker
    ) {
        var pos = (CommonTypes.BlockPos_Latest) translatedThis.pos;

        SPacketPlayerActionResponse_1_18_2.Action diggingAction = switch (translatedThis.action) {
            case START_DESTROY_BLOCK -> SPacketPlayerActionResponse_1_18_2.Action.STARTED_DIGGING;
            case ABORT_DESTROY_BLOCK -> SPacketPlayerActionResponse_1_18_2.Action.CANCELED_DIGGING;
            case STOP_DESTROY_BLOCK -> SPacketPlayerActionResponse_1_18_2.Action.FINISHED_DIGGING;
            default -> null;
        };
        if (diggingAction != null) {
            diggingTracker.pos2SequenceId().put(new DiggingTracker.DiggingPos(diggingAction, pos.packedData), sequence);
        }

        return translatedThis;
    }


    @NetworkEnum
    public enum Action {
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM,
        SWAP_ITEM_WITH_OFFHAND,
    }
}
