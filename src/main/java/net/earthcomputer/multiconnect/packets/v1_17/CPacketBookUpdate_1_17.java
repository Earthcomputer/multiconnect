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
import net.earthcomputer.multiconnect.packets.latest.ItemStack_Latest;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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
            @FilledArgument ClientPacketListener networkHandler,
            @DefaultConstruct ItemStack_Latest.NonEmpty stack
    ) {
        stack.itemId = writableBookId;
        stack.tag = new CompoundTag();
        if (!pages.isEmpty()) {
            ListTag pagesNbt = new ListTag();
            pages.stream().map(StringTag::valueOf).forEach(pagesNbt::add);
            stack.tag.put("pages", pagesNbt);
        }
        if (title.isPresent()) {
            stack.tag.put("author", StringTag.valueOf(networkHandler.getLocalGameProfile().getName()));
            stack.tag.put("title", StringTag.valueOf(title.get().trim()));
        }
        return stack;
    }

    public static boolean computeSign(@Argument("title") Optional<String> title) {
        return title.isPresent();
    }
}
