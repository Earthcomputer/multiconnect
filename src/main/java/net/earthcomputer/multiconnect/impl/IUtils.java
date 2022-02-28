package net.earthcomputer.multiconnect.impl;

import com.mojang.datafixers.DSL;
import com.mojang.serialization.DynamicOps;
import net.earthcomputer.multiconnect.protocols.generic.PacketInfo;
import net.earthcomputer.multiconnect.protocols.generic.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public interface IUtils {

    default NbtCompound datafix(DSL.TypeReference type, NbtCompound old) {
        return Utils.datafix(type, old);
    }

    default <T> T datafix(DSL.TypeReference type, DynamicOps<T> ops, T old) {
        return Utils.datafix(type, ops, old);
    }

    @SuppressWarnings("unchecked")
    default <T, U> Comparator<T> orderBy(Function<T, U> mapper, U... order) {
        return Utils.orderBy(mapper, order);
    }

    default void insertAfter(List<PacketInfo<?>> list, Class<? extends Packet<?>> element, PacketInfo<?> toInsert) {
        Utils.insertAfter(list, element, toInsert);
    }

    default void remove(List<PacketInfo<?>> list, Class<? extends Packet<?>> element) {
        Utils.remove(list, element);
    }

    default void removeTrackedDataHandler(TrackedDataHandler<?> handler) {
        Utils.removeTrackedDataHandler(handler);
    }

    default void copyBlocks(TagRegistry<Item> tags, TagRegistry<Block> blockTags, TagKey<Item> tag, TagKey<Block> blockTag) {
        Utils.copyBlocks(tags, blockTags, tag, blockTag);
    }

    default <T> int getUnmodifiedId(Registry<T> registry, T value) {
        return Utils.getUnmodifiedId(registry, value);
    }

    default <T> Identifier getUnmodifiedName(Registry<T> registry, T value) {
        return Utils.getUnmodifiedName(registry, value);
    }

    default void dumpBlockStates() {
        DebugUtils.dumpBlockStates();
    }

    @Contract("null -> fail")
    default void checkConnectionValid(@Nullable ClientPlayNetworkHandler networkHandler) {
        Utils.checkConnectionValid(networkHandler);
    }
}
