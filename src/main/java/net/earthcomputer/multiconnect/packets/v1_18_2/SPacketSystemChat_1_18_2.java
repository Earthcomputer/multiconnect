package net.earthcomputer.multiconnect.packets.v1_18_2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.ReturnType;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketSystemChat;
import net.earthcomputer.multiconnect.packets.v1_19.SPacketSystemChat_1_19;
import net.earthcomputer.multiconnect.packets.v1_19.SPacketPlayerChat_1_19;
import net.earthcomputer.multiconnect.protocols.generic.Key;
import net.earthcomputer.multiconnect.protocols.generic.TypedMap;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ChatType;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@MessageVariant(minVersion = Protocols.V1_16, maxVersion = Protocols.V1_18_2)
public class SPacketSystemChat_1_18_2 implements SPacketSystemChat {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(MyText.Arg.class, new MyText.Arg.Serializer())
            .registerTypeAdapter(MyText.Contents.class, new MyText.Contents.Serializer())
            .create();
    public static final Key<Byte> POSITION = Key.create("position");

    public CommonTypes.Text text;
    public byte position;
    @Introduce(compute = "computeSender")
    public UUID sender;

    public static UUID computeSender(@Argument("text") CommonTypes.Text text) {
        MyText myText;
        try {
            myText = GsonHelper.fromJson(GSON, text.getJson(), MyText.class, false);
        } catch (JsonParseException e) {
            return Util.NIL_UUID;
        }
        if (myText == null) {
            return Util.NIL_UUID;
        }
        Integer senderIndex = getSenderIndex(myText);
        if (senderIndex == null || senderIndex >= myText.with.length) {
            return Util.NIL_UUID;
        }
        MyText.Arg senderArg = myText.with[senderIndex];
        if (senderArg.hoverEvent == null) {
            return Util.NIL_UUID;
        }
        if (!"show_entity".equals(senderArg.hoverEvent.action)) {
            return Util.NIL_UUID;
        }
        if (senderArg.hoverEvent.contents != null) {
            try {
                return UUID.fromString(senderArg.hoverEvent.contents.id);
            } catch (IllegalArgumentException ignore) {
            }
        }
        if (senderArg.hoverEvent.value != null) {
            String value;
            if (senderArg.hoverEvent.value.isJsonPrimitive()) {
                value = senderArg.hoverEvent.value.getAsString();
            } else if (senderArg.hoverEvent.value.isJsonObject()) {
                JsonElement valueText = senderArg.hoverEvent.value.getAsJsonObject().get("text");
                if (valueText != null && valueText.isJsonPrimitive()) {
                    value = valueText.getAsString();
                } else {
                    return Util.NIL_UUID;
                }
            } else {
                return Util.NIL_UUID;
            }
            CompoundTag nbt;
            try {
                nbt = TagParser.parseTag(value);
            } catch (CommandSyntaxException e) {
                nbt = null;
            }
            if (nbt != null && nbt.contains("id", Tag.TAG_STRING)) {
                try {
                    return UUID.fromString(nbt.getString("id"));
                } catch (IllegalArgumentException ignore) {
                }
            }
        }

        return Util.NIL_UUID;
    }

    @Nullable
    private static Integer getSenderIndex(MyText myText) {
        return switch (myText.translate) {
            case "chat.type.text", "chat.type.announcement", "chat.message.display.incoming", "chat.type.emote" -> 0;
            case "chat.type.team.text" -> 1;
            default -> null;
        };
    }

    @ReturnType(SPacketPlayerChat_1_19.class)
    @ReturnType(SPacketSystemChat_1_19.class)
    @Handler
    public static List<Object> handle(
            @Argument("text") CommonTypes.Text text_,
            @Argument("position") byte position,
            @Argument("sender") UUID sender,
            @FilledArgument(fromVersion = Protocols.V1_18_2, toVersion = Protocols.V1_19) Function<Text_1_18_2, CommonTypes.Text_Latest> textTranslator,
            @FilledArgument TypedMap userData,
            @GlobalData @Nullable RegistryAccess registryAccess
    ) {
        if (registryAccess == null) {
            // Some servers apparently send chat messages before the game join packet. We can't handle these anymore
            return new ArrayList<>(0);
        }

        // 1.18.2 servers can send null chat messages, which don't do anything
        // 1.19 can't handle these anymore, so just drop them
        if ("null".equals(text_.getJson())) {
            return new ArrayList<>(0);
        }

        CommonTypes.Text text = textTranslator.apply((Text_1_18_2) text_);

        Registry<ChatType> chatTypeRegistry = registryAccess.registryOrThrow(Registry.CHAT_TYPE_REGISTRY);
        int chatId = chatTypeRegistry.getId(chatTypeRegistry.get(ChatType.CHAT));
        int chatType = chatId;

        List<Object> packets = new ArrayList<>(1);
        var basicPacket = new SPacketSystemChat_1_19();
        basicPacket.messageType = chatType;
        basicPacket.text = text;
        userData.put(POSITION, position);
        packets.add(basicPacket);

        if (position == 1 || position == 2) {
            // system chat
            return packets;
        }

        MyText myText;
        try {
            myText = GsonHelper.fromJson(GSON, text.getJson(), MyText.class, false);
        } catch (JsonParseException e) {
            return packets;
        }
        if (myText == null) {
            return packets;
        }

        int teamId = chatTypeRegistry.getId(chatTypeRegistry.get(ChatType.TEAM_MSG_COMMAND_INCOMING));
        chatType = switch (myText.translate) {
            case "chat.type.announcement" -> chatTypeRegistry.getId(chatTypeRegistry.get(ChatType.SAY_COMMAND));
            case "chat.message.display.incoming" -> chatTypeRegistry.getId(chatTypeRegistry.get(ChatType.MSG_COMMAND_INCOMING));
            case "chat.type.emote" -> chatTypeRegistry.getId(chatTypeRegistry.get(ChatType.EMOTE_COMMAND));
            case "chat.type.team.text" -> teamId;
            default -> chatType;
        };

        if (chatType == chatId && !"chat.type.text".equals(myText.translate)) {
            // tellraw, it's a system message
            return packets;
        }

        Integer contentIndex = switch (myText.translate) {
            case "chat.type.text", "chat.type.announcement", "chat.message.display.incoming", "chat.type.emote" -> 1;
            case "chat.type.team.text" -> 2;
            default -> null;
        };

        var packet = new SPacketPlayerChat_1_19();
        packet.signedContent = contentIndex == null || contentIndex >= myText.with.length
                ? text
                : new CommonTypes.Text_Latest(GSON.toJson(myText.with[contentIndex]));
        packet.unsignedContent = Optional.empty();
        packet.chatType = chatType;
        packet.sender = sender;
        Integer senderIndex = getSenderIndex(myText);
        packet.displayName = senderIndex == null || senderIndex >= myText.with.length
                ? new CommonTypes.Text_Latest("\"\"")
                : new CommonTypes.Text_Latest(GSON.toJson(myText.with[senderIndex]));
        packet.teamDisplayName = chatType != teamId || myText.with.length == 0
                ? Optional.empty()
                : Optional.of(new CommonTypes.Text_Latest(GSON.toJson(myText.with[0])));
        packet.timestamp = Instant.now().toEpochMilli();
        packet.salt = 0;
        packet.messageSignature = new byte[0];
        packets.clear();
        packets.add(packet);
        return packets;
    }

    private static class MyText {
        String translate = "";
        Arg[] with = new Arg[0];

        private static class Arg {
            HoverEvent hoverEvent;
            JsonElement remainder;

            private static class Serializer implements JsonSerializer<Arg>, JsonDeserializer<Arg> {
                @Override
                public Arg deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    Arg result = new Arg();
                    if (json.isJsonObject()) {
                        JsonObject jsonObj = json.getAsJsonObject();
                        if (jsonObj.has("hoverEvent")) {
                            result.hoverEvent = context.deserialize(jsonObj.get("hoverEvent"), HoverEvent.class);
                        }
                    }
                    result.remainder = json;
                    return result;
                }

                @Override
                public JsonElement serialize(Arg src, Type typeOfSrc, JsonSerializationContext context) {
                    if (src.remainder.isJsonObject()) {
                        JsonObject obj = src.remainder.getAsJsonObject();
                        if (src.hoverEvent != null) {
                            obj.add("hoverEvent", context.serialize(src.hoverEvent));
                        }
                    }
                    return src.remainder;
                }
            }
        }

        private static class HoverEvent {
            String action = "";
            JsonElement value;
            Contents contents;
        }

        private static class Contents {
            String id = "";
            JsonElement remainder;

            private static class Serializer implements JsonSerializer<Contents>, JsonDeserializer<Contents> {
                @Override
                public Contents deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    Contents result = new Contents();
                    if (json.isJsonObject()) {
                        JsonObject jsonObj = json.getAsJsonObject();
                        if (jsonObj.has("id")) {
                            result.id = context.deserialize(jsonObj.get("id"), String.class);
                        }
                    }
                    result.remainder = json;
                    return result;
                }

                @Override
                public JsonElement serialize(Contents src, Type typeOfSrc, JsonSerializationContext context) {
                    if (src.remainder.isJsonObject()) {
                        JsonObject obj = src.remainder.getAsJsonObject();
                        if (src.id != null) {
                            obj.add("id", context.serialize(src.id));
                        }
                    }
                    return src.remainder;
                }
            }
        }
    }
}
