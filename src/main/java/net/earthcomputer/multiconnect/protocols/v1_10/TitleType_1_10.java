package net.earthcomputer.multiconnect.protocols.v1_10;

import net.minecraft.network.packet.s2c.play.TitleS2CPacket;

public enum TitleType_1_10 {
    TITLE(TitleS2CPacket.Action.TITLE),
    SUBTITLE(TitleS2CPacket.Action.SUBTITLE),
    TIMES(TitleS2CPacket.Action.TIMES),
    CLEAR(TitleS2CPacket.Action.CLEAR),
    RESET(TitleS2CPacket.Action.RESET);

    private final TitleS2CPacket.Action newType;
    TitleType_1_10(TitleS2CPacket.Action newType) {
        this.newType = newType;
    }

    public TitleS2CPacket.Action getNewType() {
        return newType;
    }
}
