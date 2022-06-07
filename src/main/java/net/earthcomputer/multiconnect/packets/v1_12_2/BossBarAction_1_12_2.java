package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketBossBar;
import net.earthcomputer.multiconnect.packets.latest.SPacketBossBar_Latest;

@Polymorphic
@MessageVariant(maxVersion = Protocols.V1_12_2)
public abstract class BossBarAction_1_12_2 implements SPacketBossBar.Action {
    public int action;

    @Polymorphic(intValue = 0)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class AddAction extends BossBarAction_1_12_2 implements SPacketBossBar.AddAction {
        public CommonTypes.Text title;
        public float health;
        public SPacketBossBar_Latest.Color color;
        public SPacketBossBar_Latest.Division division;
        @Type(Types.UNSIGNED_BYTE)
        public int flags;
    }

    @Polymorphic(intValue = 1)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class RemoveAction extends BossBarAction_1_12_2 implements SPacketBossBar.RemoveAction {}

    @Polymorphic(intValue = 2)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class UpdateHealthAction extends BossBarAction_1_12_2 implements SPacketBossBar.UpdateHealthAction {
        public float health;
    }

    @Polymorphic(intValue = 3)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class UpdateTitleAction extends BossBarAction_1_12_2 implements SPacketBossBar.UpdateTitleAction {
        public CommonTypes.Text title;
    }

    @Polymorphic(intValue = 4)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class UpdateStyleAction extends BossBarAction_1_12_2 implements SPacketBossBar.UpdateStyleAction {
        public SPacketBossBar_Latest.Color color;
        public SPacketBossBar_Latest.Division division;
    }

    @Polymorphic(intValue = 5)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class UpdateFlagsAction extends BossBarAction_1_12_2 implements SPacketBossBar.UpdateFlagsAction {
        @Type(Types.UNSIGNED_BYTE)
        public int flags;
    }
}
