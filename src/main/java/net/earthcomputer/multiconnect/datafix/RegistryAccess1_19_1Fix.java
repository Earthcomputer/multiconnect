package net.earthcomputer.multiconnect.datafix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

import java.util.Map;
import java.util.stream.Stream;

public class RegistryAccess1_19_1Fix extends NewExperimentalRegistryAccessFix {
    public RegistryAccess1_19_1Fix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "1.19.1");
    }

    @Override
    protected String mapChatTypeName(String chatTypeName) {
        return switch (chatTypeName) {
            case "minecraft:msg_command" -> "minecraft:msg_command_incoming";
            case "minecraft:team_msg_command" -> "minecraft:team_msg_command_incoming";
            default -> chatTypeName;
        };
    }

    @Override
    protected Dynamic<?> translateChatType(Dynamic<?> fromDynamic) {
        fromDynamic = fromDynamic.set("chat", updateChatDecoration(fromDynamic.get("chat").orElseEmptyMap()));
        fromDynamic = fromDynamic.set("narration", updateChatDecoration(fromDynamic.get("chat").orElseEmptyMap()));
        return fromDynamic.remove("overlay");
    }

    private static <T> Dynamic<?> updateChatDecoration(Dynamic<T> dynamic) {
        return dynamic.get("decoration").result().orElseGet(() -> {
            return dynamic.createMap(Map.of(
                dynamic.createString("translation_key"), dynamic.createString("chat.type.system"),
                dynamic.createString("style"), dynamic.emptyMap(),
                dynamic.createString("parameters"), dynamic.createList(Stream.of(
                    dynamic.createString("content")
                ))
            ));
        });
    }
}
