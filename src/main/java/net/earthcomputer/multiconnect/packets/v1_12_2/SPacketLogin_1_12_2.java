package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.ReturnType;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.packets.SPacketCommands;
import net.earthcomputer.multiconnect.packets.SPacketLogin;
import net.earthcomputer.multiconnect.packets.v1_13_2.SPacketLogin_1_13_2;
import net.earthcomputer.multiconnect.packets.v1_13_2.SPacketUpdateRecipes_1_13_2;
import net.earthcomputer.multiconnect.packets.v1_13_2.SPacketUpdateTags_1_13_2;
import net.earthcomputer.multiconnect.protocols.v1_12.Protocol_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12.RecipeInfo;
import net.earthcomputer.multiconnect.protocols.v1_12.TabCompletionManager;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_12_2)
public class SPacketLogin_1_12_2 implements SPacketLogin {
    @Type(Types.INT)
    public int entityId;
    @Type(Types.UNSIGNED_BYTE)
    public int gamemode;
    @Type(Types.INT)
    public int dimensionId;
    @Type(Types.UNSIGNED_BYTE)
    public int difficulty;
    @Type(Types.UNSIGNED_BYTE)
    public int maxPlayers;
    public String genType;
    public boolean reducedDebugInfo;

    @ReturnType(SPacketLogin_1_13_2.class)
    @ReturnType(SPacketUpdateTags_1_13_2.class)
    @ReturnType(SPacketUpdateRecipes_1_13_2.class)
    @ReturnType(SPacketCommands.class)
    @Handler
    public static List<Object> handle(
            @Argument(value = "this", translate = true) SPacketLogin_1_13_2 translatedThis,
            @DefaultConstruct SPacketUpdateTags_1_13_2 synchronizeTagsPacket,
            @DefaultConstruct SPacketUpdateRecipes_1_13_2 synchronizeRecipesPacket
    ) {
        List<Object> packets = new ArrayList<>(4);
        packets.add(translatedThis);
        packets.add(synchronizeTagsPacket);

        int recipeId = 0;
        for (RecipeInfo<?> recipeInfo : ((Protocol_1_12_2) ConnectionInfo.protocol).getRecipes()) {
            synchronizeRecipesPacket.recipes.add(recipeInfo.toPacketRecipe(new ResourceLocation(String.valueOf(recipeId++))));
        }

        packets.add(synchronizeRecipesPacket);
        // TODO: command tree
        TabCompletionManager.requestCommandList();
        return packets;
    }
}
