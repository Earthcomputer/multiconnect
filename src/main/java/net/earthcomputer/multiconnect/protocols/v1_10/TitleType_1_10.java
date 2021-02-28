package net.earthcomputer.multiconnect.protocols.v1_10;

import net.earthcomputer.multiconnect.protocols.v1_16_4.TitleS2CPacket_1_16_4;

public enum TitleType_1_10 {
    TITLE(TitleS2CPacket_1_16_4.Type.TITLE),
    SUBTITLE(TitleS2CPacket_1_16_4.Type.SUBTITLE),
    TIMES(TitleS2CPacket_1_16_4.Type.TIMES),
    CLEAR(TitleS2CPacket_1_16_4.Type.CLEAR),
    RESET(TitleS2CPacket_1_16_4.Type.RESET);

    private final TitleS2CPacket_1_16_4.Type newType;
    TitleType_1_10(TitleS2CPacket_1_16_4.Type newType) {
        this.newType = newType;
    }

    public TitleS2CPacket_1_16_4.Type getNewType() {
        return newType;
    }
}
