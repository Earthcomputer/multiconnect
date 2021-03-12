package net.earthcomputer.multiconnect.protocols.v1_16_4;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.EndCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterCombatS2CPacket;
import net.minecraft.text.Text;

public class CombatEventS2CPacket_1_16_4 implements Packet<ClientPlayPacketListener> {
    private final Mode mode;
    private final int playerId;
    private final int killerId;
    private final int duration;
    private final Text message;

    public CombatEventS2CPacket_1_16_4(PacketByteBuf buf) {
        this.mode = buf.readEnumConstant(Mode.class);
        if (mode == Mode.END_COMBAT) {
            this.duration = buf.readVarInt();
            this.killerId = buf.readInt();
            this.playerId = -1;
            this.message = null;
        } else if (mode == Mode.ENTITY_DIED) {
            this.playerId = buf.readVarInt();
            this.killerId = buf.readInt();
            this.message = buf.readText();
            this.duration = -1;
        } else {
            this.playerId = -1;
            this.killerId = -1;
            this.duration = -1;
            this.message = null;
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        switch (mode) {
            case ENTER_COMBAT:
                listener.onEnterCombat(new EnterCombatS2CPacket());
                break;
            case END_COMBAT:
                listener.onEndCombat(new EndCombatS2CPacket(killerId, duration));
                break;
            case ENTITY_DIED:
                listener.onDeathMessage(new DeathMessageS2CPacket(playerId, killerId, message));
                break;
        }
    }

    public enum Mode {
        ENTER_COMBAT, END_COMBAT, ENTITY_DIED
    }
}
