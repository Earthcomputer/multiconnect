package net.earthcomputer.multiconnect.impl.via.provider;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.HandItemProvider;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslatorApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;

public class MulticonnectHandItemProvider extends HandItemProvider {
    private Item handItem = new DataItem(0, (byte) 0, (short) 0, null);

    public MulticonnectHandItemProvider(IMulticonnectTranslatorApi api) {
        api.scheduleRepeatingWeak(this, MulticonnectHandItemProvider::tick);
    }

    @Override
    public Item getHandItem(UserConnection info) {
        Item item = new DataItem(handItem);
        for (var protocol : info.getProtocolInfo().getPipeline().pipes()) {
            var itemRewriter = protocol.getItemRewriter();
            if (itemRewriter != null) {
                item = itemRewriter.handleItemToServer(item);
            }
        }
        return item;
    }

    private void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack stack = player.getMainHandItem();
            int id = Registry.ITEM.getId(stack.getItem());
            handItem = new DataItem(id, (byte) stack.getCount(), (short) 0, null);
        }
    }
}
