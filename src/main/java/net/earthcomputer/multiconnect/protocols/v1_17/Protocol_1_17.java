package net.earthcomputer.multiconnect.protocols.v1_17;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.PacketInfo;
import net.earthcomputer.multiconnect.protocols.v1_17_1.Protocol_1_17_1;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class Protocol_1_17 extends Protocol_1_17_1 {
    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(ScreenHandlerSlotUpdateS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readByte(); // sync id
            buf.disablePassthroughMode();
            buf.pendingRead(VarInt.class, new VarInt(0)); // revision
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(InventoryS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readUnsignedByte(); // sync id
            buf.disablePassthroughMode();
            int numItems = buf.readShort();
            List<ItemStack> items = DefaultedList.ofSize(numItems, ItemStack.EMPTY);
            for (int i = 0; i < numItems; i++) {
                items.set(i, buf.readItemStack());
            }
            buf.pendingRead(VarInt.class, new VarInt(0)); // revision
            //noinspection unchecked
            buf.pendingReadCollection((Class<Collection<ItemStack>>) (Class<?>) Collection.class, ItemStack.class, items);
            buf.pendingRead(ItemStack.class, ItemStack.EMPTY); // cursor stack
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(EntityDestroyS2CPacket.class, buf -> {
            int numEntities = buf.readVarInt();
            IntList entities = new IntArrayList(numEntities);
            for (int i = 0; i < numEntities; i++) {
                entities.add(buf.readVarInt());
            }
            buf.pendingRead(IntList.class, entities);
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerOutboundTranslator(BookUpdateC2SPacket.class, buf -> {
            Supplier<VarInt> slot = buf.skipWrite(VarInt.class);
            //noinspection unchecked
            var pages = buf.skipWriteCollection((Class<Collection<String>>) (Class<?>) Collection.class, String.class);
            var title = buf.skipWriteOptional(String.class);
            buf.pendingWrite(ItemStack.class, () -> createBookItemStack(title.get(), pages.get(), MinecraftClient.getInstance().getNetworkHandler()), buf::writeItemStack); // book
            buf.pendingWrite(Boolean.class, () -> title.get().isPresent(), buf::writeBoolean); // sign
            buf.pendingWrite(VarInt.class, slot, varInt -> buf.writeVarInt(varInt.get()));
        });
        ProtocolRegistry.registerOutboundTranslator(ClickSlotC2SPacket.class, buf -> {
            buf.passthroughWrite(Byte.class); // sync id
            buf.skipWrite(VarInt.class); // revision
        });
    }

    public static ItemStack createBookItemStack(Optional<String> title, Collection<String> pages, @Nullable ClientPlayNetworkHandler connection) {
        ItemStack bookStack = new ItemStack(Items.WRITABLE_BOOK);
        if (!pages.isEmpty()) {
            NbtList pagesNbt = new NbtList();
            pages.stream().map(NbtString::of).forEach(pagesNbt::add);
            bookStack.putSubTag("pages", pagesNbt);
        }
        if (title.isPresent()) {
            bookStack.putSubTag("author", NbtString.of(connection == null ? "" : connection.getProfile().getName()));
            bookStack.putSubTag("title", NbtString.of(title.get().trim()));
        }
        return bookStack;
    }

    @Override
    public List<PacketInfo<?>> getClientboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        insertAfter(packets, EntityDestroyS2CPacket.class, PacketInfo.of(EntityDestroyS2CPacket_1_17.class, EntityDestroyS2CPacket_1_17::new));
        remove(packets, EntityDestroyS2CPacket.class);
        return packets;
    }
}
