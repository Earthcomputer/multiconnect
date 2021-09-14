package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class CPacketBoatPaddleState {
    public boolean leftPaddleTurning;
    public boolean rightPaddleTurning;
}
