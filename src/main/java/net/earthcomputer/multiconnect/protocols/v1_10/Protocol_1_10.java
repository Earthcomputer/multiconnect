package net.earthcomputer.multiconnect.protocols.v1_10;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.v1_11.Protocol_1_11;
import net.earthcomputer.multiconnect.protocols.v1_12.command.BrigadierRemover;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class Protocol_1_10 extends Protocol_1_11 {
    @Override
    public void registerCommands(CommandBuildContext context, CommandDispatcher<SharedSuggestionProvider> dispatcher, @Nullable Set<String> serverCommands) {
        super.registerCommands(context, dispatcher, serverCommands);
        BrigadierRemover.of(dispatcher).get("locate").remove();
        BrigadierRemover.of(dispatcher).get("title").get("player").get("actionbar").remove();
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
