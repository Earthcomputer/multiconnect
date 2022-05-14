package net.earthcomputer.multiconnect.packets.v1_17;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketBookUpdate;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.List;
import java.util.Optional;

@MessageVariant(minVersion = Protocols.V1_16_5, maxVersion = Protocols.V1_17)
public class CPacketBookUpdate_1_17 implements CPacketBookUpdate {
    @Introduce(compute = "computeStack")
    public CommonTypes.ItemStack stack;
    @Introduce(compute = "computeSign")
    public boolean sign;
    public int slot;

    public static CommonTypes.ItemStack computeStack(
            @Argument("pages") List<String> pages,
            @Argument("title") Optional<String> title,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "writable_book")) int writableBookId,
            @FilledArgument ClientPlayNetworkHandler networkHandler,
            @DefaultConstruct CommonTypes.NonEmptyItemStack stack
    ) {
        stack.itemId = writableBookId;
        stack.tag = new NbtCompound();
        if (!pages.isEmpty()) {
            NbtList pagesNbt = new NbtList();
            pages.stream().map(NbtString::of).forEach(pagesNbt::add);
            stack.tag.put("pages", pagesNbt);
        }
        if (title.isPresent()) {
            stack.tag.put("author", NbtString.of(networkHandler.getProfile().getName()));
            stack.tag.put("title", NbtString.of(title.get().trim()));
        }
        return stack;
    }

    public static boolean computeSign(@Argument("title") Optional<String> title) {
        return title.isPresent();
    }
}
