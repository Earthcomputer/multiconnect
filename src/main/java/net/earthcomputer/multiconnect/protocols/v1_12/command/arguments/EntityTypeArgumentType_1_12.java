package net.earthcomputer.multiconnect.protocols.v1_12.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
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
            if (type == null || !type.canSummon()) {
                reader.setCursor(start);
                throw ResourceArgument.ERROR_UNKNOWN_RESOURCE.createWithContext(reader, entityId, Registries.ENTITY_TYPE.location());
            }
            return entityId;
        } else {
            ResourceLocation entityId = ResourceLocation.read(reader);
            return BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).filter(EntityType::canSummon).orElseThrow(() -> {
                reader.setCursor(start);
                return ResourceArgument.ERROR_UNKNOWN_RESOURCE.createWithContext(reader, entityId, Registries.ENTITY_TYPE.location());
            }).toString();
        }
    }

    public Collection<String> getExamples() {
        return ConnectionInfo.protocolVersion <= Protocols.V1_10 ? EXAMPLES_1_10 : EXAMPLES;
    }
}
