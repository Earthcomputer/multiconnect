package net.earthcomputer.multiconnect.packets.latest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketPlayerChat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@MessageVariant(minVersion = Protocols.V1_19_1)
public class SPacketPlayerChat_Latest implements SPacketPlayerChat {
    private static final Gson GSON = new Gson();

    @Introduce(compute = "computeHeader")
    public SignedHeader header;
    public byte[] messageSignature;
    @Introduce(compute = "computeSignedBody")
    public SignedBody signedBody;
    public Optional<CommonTypes.Text> unsignedContent;
    public int chatType;
    public CommonTypes.Text displayName;
    public Optional<CommonTypes.Text> teamDisplayName;

    public static SignedHeader computeHeader(@Argument("sender") UUID sender) {
        SignedHeader header = new SignedHeader();
        header.previousSignature = Optional.empty();
        header.sender = sender;
        return header;
    }

    public static SignedBody computeSignedBody(
        @Argument("signedContent") CommonTypes.Text signedContent,
        @Argument("timestamp") long timestamp,
        @Argument("salt") long salt
    ) {
        JsonElement json = GSON.fromJson(signedContent.getJson(), JsonElement.class);
        String plain = signedContent.getJson();
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            if (!jsonArray.isEmpty()) {
                json = jsonArray.get(0);
            }
        }
        if (json.isJsonPrimitive()) {
            plain = json.getAsString();
        } else if (json.isJsonObject()) {
            JsonElement text = json.getAsJsonObject().get("text");
            if (text != null && text.isJsonPrimitive()) {
                plain = text.getAsString();
            }
        }

        SignedBody signedBody = new SignedBody();
        signedBody.content = new ChatContent();
        signedBody.content.plain = plain;
        signedBody.content.decorated = Optional.of(signedContent);
        signedBody.timestamp = timestamp;
        signedBody.salt = salt;
        signedBody.lastSeen = new ArrayList<>(0);
        return signedBody;
    }

    @MessageVariant
    public static class SignedHeader {
        public Optional<byte[]> previousSignature;
        public UUID sender;
    }

    @MessageVariant
    public static class SignedBody {
        public ChatContent content;
        @Type(Types.LONG)
        public long timestamp;
        @Type(Types.LONG)
        public long salt;
        @Length(max = 5)
        public List<LastSeenMessage> lastSeen;
    }

    @MessageVariant
    public static class ChatContent {
        @Length(max = 256)
        public String plain;
        public Optional<CommonTypes.Text> decorated;
    }

    @MessageVariant
    public static class LastSeenMessage {
        public UUID uuid;
        public byte[] signature;
    }

    @MessageVariant
    public static class LastSeenUpdate {
        @Length(max = 5)
        public List<SPacketPlayerChat_Latest.LastSeenMessage> lastSeen;
        public Optional<SPacketPlayerChat_Latest.LastSeenMessage> lastReceived;
    }
}
