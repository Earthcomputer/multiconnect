package net.earthcomputer.multiconnect.protocols.v1_10;

import net.earthcomputer.multiconnect.api.ProtocolBehavior;
import net.earthcomputer.multiconnect.protocols.v1_12.command.BrigadierRemover;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public class Protocol_1_10 extends ProtocolBehavior {
    @Override
    public void onCommandRegistration(CommandRegistrationArgs args) {
        BrigadierRemover.of(args.dispatcher()).get("locate").remove();
        BrigadierRemover.of(args.dispatcher()).get("title").get("player").get("actionbar").remove();
    }

    @Nullable
    public static EntityType<?> getEntityById(String id) {
        // TODO: rewrite for via
        return null;
    }

    public static String getEntityId(EntityType<?> entityType) {
        // TODO: rewrite for via
        return String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
    }
}
