package net.earthcomputer.multiconnect.protocols.v1_8;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

public enum ClientCommandMode_1_8 {
    PRESS_SHIFT_KEY,
    RELEASE_SHIFT_KEY,
    STOP_SLEEPING,
    START_SPRINTING,
    STOP_SPRINTING,
    HORSE_JUMP,
    OPEN_INVENTORY;

    public static ClientCommandMode_1_8 fromNew(ClientCommandC2SPacket.Mode mode) {
        switch (mode) {
            case PRESS_SHIFT_KEY: return PRESS_SHIFT_KEY;
            case RELEASE_SHIFT_KEY: return RELEASE_SHIFT_KEY;
            case STOP_SLEEPING: return STOP_SLEEPING;
            case START_SPRINTING: return START_SPRINTING;
            case STOP_SPRINTING: return STOP_SPRINTING;
            case START_RIDING_JUMP: return HORSE_JUMP;
            case OPEN_INVENTORY:
            default:
                return OPEN_INVENTORY;
        }
    }
}
