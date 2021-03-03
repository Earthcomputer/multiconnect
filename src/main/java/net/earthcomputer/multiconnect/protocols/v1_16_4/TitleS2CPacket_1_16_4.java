package net.earthcomputer.multiconnect.protocols.v1_16_4;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.text.Text;

public class TitleS2CPacket_1_16_4 implements Packet<ClientPlayPacketListener> {
    private final Type type;
    private final Text text;
    private final int fadeInTime;
    private final int stayTime;
    private final int fadeOutTime;

    public TitleS2CPacket_1_16_4(PacketByteBuf buf) {
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
            case TITLE:
                listener.onTitle(new TitleS2CPacket(text));
                break;
            case SUBTITLE:
                listener.onSubtitle(new SubtitleS2CPacket(text));
                break;
            case ACTIONBAR:
                listener.onOverlayMessage(new OverlayMessageS2CPacket(text));
                break;
            case TIMES:
                listener.onTitleFade(new TitleFadeS2CPacket(fadeInTime, stayTime, fadeOutTime));
                break;
            case CLEAR:
                listener.onTitleClear(new ClearTitleS2CPacket(false));
                break;
            case RESET:
                listener.onTitleClear(new ClearTitleS2CPacket(true));
                break;
        }
    }

    public enum Type {
        TITLE, SUBTITLE, ACTIONBAR, TIMES, CLEAR, RESET
    }
}
