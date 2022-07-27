package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketSystemChat;
import net.earthcomputer.multiconnect.packets.v1_18_2.SPacketSystemChat_1_18_2;
import net.earthcomputer.multiconnect.protocols.generic.TypedMap;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@MessageVariant(minVersion = Protocols.V1_19_1)
public class SPacketSystemChat_Latest implements SPacketSystemChat {
    private static final ResourceKey<ChatType> GAME_INFO_TYPE = ResourceKey.create(Registry.CHAT_TYPE_REGISTRY, new ResourceLocation("game_info"));

    public CommonTypes.Text text;
    @Introduce(compute = "computeOverlay")
    public boolean overlay;

    public static boolean computeOverlay(
        @Argument("messageType") int messageType,
        @FilledArgument TypedMap userData,
        @GlobalData @Nullable RegistryAccess registryAccess
    ) {
        Byte position = userData.get(SPacketSystemChat_1_18_2.POSITION);
        if (position != null) {
            return position == 2;
        }

        if (registryAccess == null) {
            return false;
        }
        var chatTypeRegistry = registryAccess.registryOrThrow(Registry.CHAT_TYPE_REGISTRY);
        ChatType chatType = chatTypeRegistry.byId(messageType);
        if (chatType == null) {
            return false;
        }
        var resourceKey = chatTypeRegistry.getResourceKey(chatType);
        if (resourceKey.isEmpty()) {
            return false;
        }
        return resourceKey.get() == GAME_INFO_TYPE;
    }
}
