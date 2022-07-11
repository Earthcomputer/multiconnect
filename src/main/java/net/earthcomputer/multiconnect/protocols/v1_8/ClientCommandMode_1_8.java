package net.earthcomputer.multiconnect.protocols.v1_8;

import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public enum ClientCommandMode_1_8 {
    PRESS_SHIFT_KEY,
    RELEASE_SHIFT_KEY,
    STOP_SLEEPING,
    START_SPRINTING,
    STOP_SPRINTING,
    HORSE_JUMP,
    OPEN_INVENTORY;

    public static ClientCommandMode_1_8 fromNew(ServerboundPlayerCommandPacket.Action mode) {
        return switch (mode) {
            case PRESS_SHIFT_KEY -> PRESS_SHIFT_KEY;
            case RELEASE_SHIFT_KEY -> RELEASE_SHIFT_KEY;
            case STOP_SLEEPING -> STOP_SLEEPING;
            case START_SPRINTING -> START_SPRINTING;
            case STOP_SPRINTING -> STOP_SPRINTING;
            case START_RIDING_JUMP -> HORSE_JUMP;
            default -> OPEN_INVENTORY;
        };
    }
}
