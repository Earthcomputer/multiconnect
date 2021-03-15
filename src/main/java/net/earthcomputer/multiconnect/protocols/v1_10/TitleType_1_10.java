package net.earthcomputer.multiconnect.protocols.v1_10;

import net.earthcomputer.multiconnect.protocols.v1_16_5.TitleS2CPacket_1_16_5;

public enum TitleType_1_10 {
    TITLE(TitleS2CPacket_1_16_5.Type.TITLE),
    SUBTITLE(TitleS2CPacket_1_16_5.Type.SUBTITLE),
    TIMES(TitleS2CPacket_1_16_5.Type.TIMES),
    CLEAR(TitleS2CPacket_1_16_5.Type.CLEAR),
    RESET(TitleS2CPacket_1_16_5.Type.RESET);

    private final TitleS2CPacket_1_16_5.Type newType;
    TitleType_1_10(TitleS2CPacket_1_16_5.Type newType) {
        this.newType = newType;
    }

    public TitleS2CPacket_1_16_5.Type getNewType() {
        return newType;
    }
}
