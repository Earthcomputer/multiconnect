package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@MessageVariant(maxVersion = Protocols.V1_18_2)
public class Text_1_18_2 implements CommonTypes.Text {
    @Length(max = FriendlyByteBuf.MAX_COMPONENT_STRING_LENGTH)
    public String json;

    public Text_1_18_2() {
    }

    public Text_1_18_2(String json) {
        this.json = json;
    }

    @Override
    public String getJson() {
        return json;
    }

    public static Text_1_18_2 createLiteral(String value) {
        var text = Component.literal(value);
        String json = Component.Serializer.toJson(text);
        return new Text_1_18_2(json);
    }
}
