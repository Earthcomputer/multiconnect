package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketBossEvent;
import net.earthcomputer.multiconnect.packets.latest.SPacketBossEvent_Latest;

@Polymorphic
@MessageVariant(maxVersion = Protocols.V1_12_2)
public abstract class BossBarAction_1_12_2 implements SPacketBossEvent.Action {
    public int action;

    @Polymorphic(intValue = 0)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class AddAction extends BossBarAction_1_12_2 implements SPacketBossEvent.AddAction {
        public CommonTypes.Text title;
        public float health;
        public SPacketBossEvent_Latest.Color color;
        public SPacketBossEvent_Latest.Division division;
        @Type(Types.UNSIGNED_BYTE)
        public int flags;
    }

    @Polymorphic(intValue = 1)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class RemoveAction extends BossBarAction_1_12_2 implements SPacketBossEvent.RemoveAction {}

    @Polymorphic(intValue = 2)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class UpdateHealthAction extends BossBarAction_1_12_2 implements SPacketBossEvent.UpdateHealthAction {
        public float health;
    }

    @Polymorphic(intValue = 3)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class UpdateTitleAction extends BossBarAction_1_12_2 implements SPacketBossEvent.UpdateTitleAction {
        public CommonTypes.Text title;
    }

    @Polymorphic(intValue = 4)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class UpdateStyleAction extends BossBarAction_1_12_2 implements SPacketBossEvent.UpdateStyleAction {
        public SPacketBossEvent_Latest.Color color;
        public SPacketBossEvent_Latest.Division division;
    }

    @Polymorphic(intValue = 5)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class UpdateFlagsAction extends BossBarAction_1_12_2 implements SPacketBossEvent.UpdateFlagsAction {
        @Type(Types.UNSIGNED_BYTE)
        public int flags;
    }
}
