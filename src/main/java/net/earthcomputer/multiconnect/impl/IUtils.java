package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.protocols.generic.ISimpleRegistry;
import net.earthcomputer.multiconnect.protocols.generic.PacketInfo;
import net.earthcomputer.multiconnect.protocols.generic.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.class_5455;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public interface IUtils {
    @SuppressWarnings("unchecked")
    default <T, U> Comparator<T> orderBy(Function<T, U> mapper, U... order) {
        return Utils.orderBy(mapper, order);
    }

    default void insertAfter(List<PacketInfo<?>> list, Class<? extends Packet<?>> element, PacketInfo<?> toInsert) {
        Utils.insertAfter(list, element, toInsert);
    }

    default <T> void insertAfter(List<T> list, T element, T toInsert) {
        Utils.insertAfter(list, element, toInsert);
    }

    default <T> void insertAfter(ISimpleRegistry<T> registry, T element, T toInsert, String id) {
        Utils.insertAfter(registry, element, toInsert, id);
    }

    default <T> void insertAfter(ISimpleRegistry<T> registry, T element, T toInsert, String id, boolean inPlace) {
        Utils.insertAfter(registry, element, toInsert, id, inPlace);
    }

    default void remove(List<PacketInfo<?>> list, Class<? extends Packet<?>> element) {
        Utils.remove(list, element);
    }

    default void removeTrackedDataHandler(TrackedDataHandler<?> handler) {
        Utils.removeTrackedDataHandler(handler);
    }

    default void copyBlocks(TagRegistry<Item> tags, TagRegistry<Block> blockTags, Tag.Identified<Item> tag, Tag.Identified<Block> blockTag) {
        Utils.copyBlocks(tags, blockTags, tag, blockTag);
    }

    default <T> int getUnmodifiedId(Registry<T> registry, T value) {
        return Utils.getUnmodifiedId(registry, value);
    }

    default <T> Identifier getUnmodifiedName(Registry<T> registry, T value) {
        return Utils.getUnmodifiedName(registry, value);
    }

    default <T> void rename(ISimpleRegistry<T> registry, T value, String newName) {
        Utils.rename(registry, value, newName);
    }

    default <T> void reregister(ISimpleRegistry<T> registry, T value, boolean inPlace) {
        Utils.reregister(registry, value, inPlace);
    }

    default <T, R extends Registry<T>> void addRegistry(class_5455.class_5457 registries, RegistryKey<R> registryKey) {
        Utils.addRegistry(registries, registryKey);
    }

    default void dumpBlockStates() {
        Utils.dumpBlockStates();
    }
}