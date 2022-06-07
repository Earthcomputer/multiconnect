package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class CPacketBoatPaddleState {
    public boolean leftPaddleTurning;
    public boolean rightPaddleTurning;
}
