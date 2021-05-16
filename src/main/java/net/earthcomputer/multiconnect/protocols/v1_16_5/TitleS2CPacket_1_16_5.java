package net.earthcomputer.multiconnect.protocols.v1_16_5;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.text.Text;

public class TitleS2CPacket_1_16_5 implements Packet<ClientPlayPacketListener> {
    private final Type type;
    private final Text text;
    private final int fadeInTime;
    private final int stayTime;
    private final int fadeOutTime;

    public TitleS2CPacket_1_16_5(PacketByteBuf buf) {
        this.type = buf.readEnumConstant(Type.class);
        if (type == Type.TITLE || type == Type.SUBTITLE || type == Type.ACTIONBAR) {
            this.text = buf.readText();
        } else {
            this.text = null;
        }
        if (type == Type.TIMES) {
            fadeInTime = buf.readInt();
            stayTime = buf.readInt();
            fadeOutTime = buf.readInt();
        } else {
            fadeInTime = stayTime = fadeOutTime = -1;
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        switch (type) {
            case TITLE -> listener.onTitle(new TitleS2CPacket(text));
            case SUBTITLE -> listener.onSubtitle(new SubtitleS2CPacket(text));
            case ACTIONBAR -> listener.onOverlayMessage(new OverlayMessageS2CPacket(text));
            case TIMES -> listener.onTitleFade(new TitleFadeS2CPacket(fadeInTime, stayTime, fadeOutTime));
            case CLEAR -> listener.onTitleClear(new ClearTitleS2CPacket(false));
            case RESET -> listener.onTitleClear(new ClearTitleS2CPacket(true));
        }
    }

    public enum Type {
        TITLE, SUBTITLE, ACTIONBAR, TIMES, CLEAR, RESET
    }
}
