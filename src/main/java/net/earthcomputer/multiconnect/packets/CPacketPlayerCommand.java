package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;

@MessageVariant
public class CPacketPlayerCommand {
    public int playerId;
    public Action action;
    public int horseJumpBoost;

    @NetworkEnum
    public enum Action {
        PRESS_SHIFT_KEY,
        RELEASE_SHIFT_KEY,
        STOP_SLEEPING,
        START_SPRINTING,
        STOP_SPRINTING,
        START_RIDING_JUMP,
        STOP_RIDING_JUMP,
        OPEN_INVENTORY,
        START_FALL_FLYING,
    }
}
