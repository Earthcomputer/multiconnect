package net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.minecraft.command.arguments.*;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.Collection;

public class EntityTypeArgumentType_1_12 implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:pig", "cow");
    private static final Collection<String> EXAMPLES_1_10 = Arrays.asList("Pig", "Cow");

    public static EntityTypeArgumentType_1_12 entityType() {
        return new EntityTypeArgumentType_1_12();
    }

    public String parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        if (ConnectionInfo.protocolVersion <= Protocols.V1_10) {
            String entityId = reader.readUnquotedString();
            EntityType<?> type = Protocol_1_10.getEntityById(entityId);
            if (type == null || !type.isSummonable()) {
                reader.setCursor(start);
                throw EntitySummonArgumentType.NOT_FOUND_EXCEPTION.createWithContext(reader, entityId);
            }
            return entityId;
        } else {
            Identifier entityId = Identifier.fromCommandInput(reader);
            return Registry.ENTITY_TYPE.getOrEmpty(entityId).filter(EntityType::isSummonable).orElseThrow(() -> {
                reader.setCursor(start);
                return EntitySummonArgumentType.NOT_FOUND_EXCEPTION.createWithContext(reader, entityId);
            }).toString();
        }
    }

    public Collection<String> getExamples() {
        return ConnectionInfo.protocolVersion <= Protocols.V1_10 ? EXAMPLES_1_10 : EXAMPLES;
    }
}
