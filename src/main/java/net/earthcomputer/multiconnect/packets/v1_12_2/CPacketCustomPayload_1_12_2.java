package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketCustomPayload;
import net.earthcomputer.multiconnect.packets.CommonTypes;

@Polymorphic
@MessageVariant(maxVersion = Protocols.V1_12_2)
public abstract class CPacketCustomPayload_1_12_2 implements CPacketCustomPayload {
    @Introduce(stringValue = "MC|Brand") // the only channel translated here is minecraft:brand
    public String channel;

    @Polymorphic(stringValue = "MC|Brand")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class Brand extends CPacketCustomPayload_1_12_2 implements CPacketCustomPayload.Brand {
        public String brand;
    }

    @Polymorphic(stringValue = "MC|BEdit")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class BookEdit extends CPacketCustomPayload_1_12_2 implements CPacketCustomPayload.BookEdit {
        public CommonTypes.ItemStack stack;
    }

    @Polymorphic(stringValue = "MC|BSign")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class BookSign extends CPacketCustomPayload_1_12_2 implements CPacketCustomPayload.BookSign {
        public CommonTypes.ItemStack stack;
    }

    @Polymorphic(stringValue = "MC|PickItem")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class PickItem extends CPacketCustomPayload_1_12_2 implements CPacketCustomPayload.PickItem {
        public int slot;
    }

    @Polymorphic(stringValue = "MC|ItemName")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class ItemName extends CPacketCustomPayload_1_12_2 implements CPacketCustomPayload.ItemName {
        public String name;
    }

    @Polymorphic(stringValue = "MC|TrSel")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class TradeSelect extends CPacketCustomPayload_1_12_2 implements CPacketCustomPayload.TradeSelect {
        @Type(Types.INT)
        public int slot;
    }

    @Polymorphic(stringValue = "MC|Beacon")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class Beacon extends CPacketCustomPayload_1_12_2 implements CPacketCustomPayload.Beacon {
        @Type(Types.INT)
        public int primaryEffect;
        @Type(Types.INT)
        public int secondaryEffect;
    }

    @Polymorphic(stringValue = "MC|AutoCmd")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class AutoCmd extends CPacketCustomPayload_1_12_2 implements CPacketCustomPayload.AutoCmd {
        @Type(Types.INT)
        public int x;
        @Type(Types.INT)
        public int y;
        @Type(Types.INT)
        public int z;
        public String command;
        public boolean trackOutput;
        public String mode;
        public boolean conditional;
        public boolean alwaysActive;
    }

    @Polymorphic(stringValue = "MC|AdvCmd")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class AdvCmd extends CPacketCustomPayload_1_12_2 implements CPacketCustomPayload.AdvCmd {
        public byte type;
        @Type(Types.INT)
        public int entityId;
        public String command;
        public boolean trackOutput;
    }

    @Polymorphic(stringValue = "MC|Struct")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class Struct extends CPacketCustomPayload_1_12_2 implements CPacketCustomPayload.Struct {
        @Type(Types.INT)
        public int x;
        @Type(Types.INT)
        public int y;
        @Type(Types.INT)
        public int z;
        public byte action;
        public String mode;
        public String structureName;
        @Type(Types.INT)
        public int offsetX;
        @Type(Types.INT)
        public int offsetY;
        @Type(Types.INT)
        public int offsetZ;
        @Type(Types.INT)
        public int sizeX;
        @Type(Types.INT)
        public int sizeY;
        @Type(Types.INT)
        public int sizeZ;
        public String mirror;
        public String rotation;
        public String metadata;
        public boolean ignoreEntities;
        public boolean showAir;
        public boolean showBoundingBox;
        public float integrity;
        public long seed;
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class Other extends CPacketCustomPayload_1_12_2 implements CPacketCustomPayload.Other {
        @Length(remainingBytes = true)
        public byte[] data;
    }
}
