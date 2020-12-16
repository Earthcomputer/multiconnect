package net.earthcomputer.multiconnect.impl;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.earthcomputer.multiconnect.api.IProtocol;
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.mixin.bridge.DynamicRegistryManagerImplAccessor;
import net.earthcomputer.multiconnect.mixin.bridge.TrackedDataHandlerRegistryAccessor;
import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_16_4.mixin.DimensionTypeAccessor;
import net.earthcomputer.multiconnect.transformer.Codecked;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.tag.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.util.registry.*;
import net.minecraft.world.dimension.DimensionType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Utils {
    public static CompoundTag datafix(DSL.TypeReference type, CompoundTag old) {
        return (CompoundTag) datafix(type, NbtOps.INSTANCE, old);
    }

    public static <T> T datafix(DSL.TypeReference type, DynamicOps<T> ops, T old) {
        int oldVersion = ConnectionMode.byValue(ConnectionInfo.protocolVersion).getDataVersion();
        int currentVersion = SharedConstants.getGameVersion().getWorldVersion();
        if (oldVersion == currentVersion) {
            return old;
        }
        DataFixer fixer = MinecraftClient.getInstance().getDataFixer();
        Dynamic<T> translated = fixer.update(type, new Dynamic<>(ops, old), oldVersion, currentVersion);
        return translated.getValue();
    }

    @SafeVarargs
    public static <T, U> Comparator<T> orderBy(Function<T, U> mapper, U... order) {
        ImmutableMap.Builder<U, Integer> indexBuilder = ImmutableMap.builder();
        for (int i = 0; i < order.length; i++) {
            indexBuilder.put(order[i], i);
        }
        ImmutableMap<U, Integer> indexes = indexBuilder.build();
        Integer absent = indexes.size();
        return Comparator.comparing(val -> indexes.getOrDefault(mapper.apply(val), absent));
    }

    public static void insertAfter(List<PacketInfo<?>> list, Class<? extends Packet<?>> element, PacketInfo<?> toInsert) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getPacketClass() == element) {
                list.add(i + 1, toInsert);
                return;
            }
        }
        list.add(0, toInsert);
    }

    public static <T> void insertAfter(List<T> list, T element, T toInsert) {
        list.add(list.indexOf(element) + 1, toInsert);
    }

    public static <T> void insertAfter(ISimpleRegistry<T> registry, T element, T toInsert, String id) {
        insertAfter(registry, element, toInsert, id, false);
    }

    @SuppressWarnings("unchecked")
    public static <T> void insertAfter(ISimpleRegistry<T> registry, T element, T toInsert, String id, boolean inPlace) {
        RegistryKey<T> key = RegistryKey.of(registry.getRegistryKey(), new Identifier(id));
        int numericalId = ((SimpleRegistry<T>) registry).getRawId(element) + 1;
        if (inPlace) {
            registry.registerInPlace(toInsert, numericalId, key);
        } else {
            registry.register(toInsert, numericalId, key);
        }
    }

    public static void remove(List<PacketInfo<?>> list, Class<? extends Packet<?>> element) {
        list.removeIf(it -> it.getPacketClass() == element);
    }

    public static void removeTrackedDataHandler(TrackedDataHandler<?> handler) {
        Int2ObjectBiMap<TrackedDataHandler<?>> biMap = TrackedDataHandlerRegistryAccessor.getHandlers();
        //noinspection unchecked
        IInt2ObjectBiMap<TrackedDataHandler<?>> iBiMap = (IInt2ObjectBiMap<TrackedDataHandler<?>>) biMap;
        int id = TrackedDataHandlerRegistry.getId(handler);
        iBiMap.multiconnect_remove(handler);
        for (; TrackedDataHandlerRegistry.get(id + 1) != null; id++) {
            TrackedDataHandler<?> h = TrackedDataHandlerRegistry.get(id + 1);
            iBiMap.multiconnect_remove(h);
            biMap.put(h, id);
        }
    }

    public static void copyBlocks(TagRegistry<Item> tags, TagRegistry<Block> blockTags, Tag.Identified<Item> tag, Tag.Identified<Block> blockTag) {
        tags.add(tag, Collections2.transform(blockTags.get(blockTag.getId()), Block::asItem));
    }

    @SuppressWarnings("unchecked")
    public static <T> int getUnmodifiedId(Registry<T> registry, T value) {
        DefaultRegistries<T> defaultRegistries = (DefaultRegistries<T>) DefaultRegistries.DEFAULT_REGISTRIES.get(registry);
        if (defaultRegistries == null) return registry.getRawId(value);
        return defaultRegistries.defaultEntryToRawId.getInt(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Identifier getUnmodifiedName(Registry<T> registry, T value) {
        DefaultRegistries<T> defaultRegistries = (DefaultRegistries<T>) DefaultRegistries.DEFAULT_REGISTRIES.get(registry);
        if (defaultRegistries == null) return registry.getId(value);
        return defaultRegistries.defaultIdToEntry.inverse().get(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> void rename(ISimpleRegistry<T> registry, T value, String newName) {
        int id = ((SimpleRegistry<T>) registry).getRawId(value);
        registry.purge(value);
        RegistryKey<T> key = RegistryKey.of(registry.getRegistryKey(), new Identifier(newName));
        registry.registerInPlace(value, id, key);
    }

    @SuppressWarnings("unchecked")
    public static <T> void rename(ISimpleRegistry<T> registry, RegistryKey<T> from, String newName) {
        rename(registry, ((SimpleRegistry<T>) registry).get(from), newName);
    }

    @SuppressWarnings("unchecked")
    public static <T> void reregister(ISimpleRegistry<T> registry, T value, boolean inPlace) {
        if (registry.getIdToEntry().containsValue(value))
            return;

        //noinspection SuspiciousMethodCalls
        DefaultRegistries<T> defaultRegistries = (DefaultRegistries<T>) DefaultRegistries.DEFAULT_REGISTRIES.get(registry);
        T prevValue = null;
        for (int id = defaultRegistries.defaultEntryToRawId.getInt(value) - 1; id >= 0; id--) {
            T val = defaultRegistries.defaultRawIdToEntry.get(id);
            if (registry.getIdToEntry().containsValue(val)) {
                prevValue = val;
                break;
            }
        }

        insertAfter(registry, prevValue, value, defaultRegistries.defaultIdToEntry.inverse().get(value).toString(), inPlace);
    }

    @SuppressWarnings("unchecked")
    public static <T, R extends Registry<T>> void addRegistry(DynamicRegistryManager.Impl registries, RegistryKey<R> registryKey) {
        //noinspection ConstantConditions
        Map<RegistryKey<? extends Registry<?>>, SimpleRegistry<?>> registryMap =
                (Map<RegistryKey<? extends Registry<?>>, SimpleRegistry<?>>) ((DynamicRegistryManagerImplAccessor) (Object) registries).getRegistries();

        if (registryMap.containsKey(registryKey)) {
            return;
        }
        SimpleRegistry<T> registry = new SimpleRegistry<>(registryKey, Lifecycle.stable());
        registryMap.putIfAbsent(registryKey, registry);
        if (registryKey == Registry.DIMENSION_TYPE_KEY) {
            DimensionType.addRegistryDefaults(registries);
        } else {
            SimpleRegistry<T> builtinRegistry = (SimpleRegistry<T>) ((Registry<R>) BuiltinRegistries.REGISTRIES).get(registryKey);
            assert builtinRegistry != null;
            for (Map.Entry<RegistryKey<T>, T> entry : builtinRegistry.getEntries()) {
                registry.set(builtinRegistry.getRawId(entry.getValue()), entry.getKey(), entry.getValue(), builtinRegistry.getEntryLifecycle(entry.getValue()));
            }
        }
    }

    public static <T> void translateDynamicRegistries(TransformerByteBuf buf, Codec<T> oldCodec, Predicate<T> allowablePredicate) {
        translateExperimentalCodec(buf, oldCodec, allowablePredicate, DynamicRegistryManager.Impl.CODEC, thing -> {
            DynamicRegistryManager.Impl registryManager = DynamicRegistryManager.create();
            //noinspection ConstantConditions
            ((DynamicRegistryManagerImplAccessor) (Object) registryManager).setRegistries(ImmutableMap.of()); // dynamic registry mutator will fix this
            return registryManager;
        });
    }

    private static final Map<Identifier, DimensionType> DIMENSION_TYPES_BY_ID = ImmutableMap.of(
            DimensionType.OVERWORLD_ID, DimensionTypeAccessor.getOverworld(),
            DimensionType.THE_NETHER_ID, DimensionTypeAccessor.getTheNether(),
            DimensionType.THE_END_ID, DimensionTypeAccessor.getTheEnd()
    );

    public static void translateDimensionType(TransformerByteBuf buf) {
        translateExperimentalCodec(
                buf,
                Utils.singletonKeyCodec("effects", Identifier.CODEC),
                DIMENSION_TYPES_BY_ID::containsKey,
                DimensionType.REGISTRY_CODEC,
                id -> () -> DIMENSION_TYPES_BY_ID.get(id)
        );
    }

    @SuppressWarnings("unchecked")
    private static <T, U> void translateExperimentalCodec(TransformerByteBuf buf, Codec<T> oldCodec, Predicate<T> allowablePredicate, Codec<U> thingCodec, Function<T, U> thingCreator) {
        // TODO: support actual translation when this format stops being experimental
        boolean[] hasDecoded = {false};
        T oldThing;
        try {
            oldThing = buf.decode(oldCodec.xmap(val -> {
                hasDecoded[0] = true;
                return val;
            }, Function.identity()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (!hasDecoded[0]) {
            // already valid
            buf.pendingRead(Codecked.class, new Codecked<>(thingCodec, (U) oldThing));
            return;
        }
        if (!allowablePredicate.test(oldThing)) {
            ClientConnection connection = buf.getConnection();
            if (connection != null) {
                connection.disconnect(new TranslatableText("multiconnect.unsupportedExperimentalCodec"));
            }
            return;
        }

        U thing = thingCreator.apply(oldThing);
        buf.pendingRead(Codecked.class, new Codecked<>(thingCodec, thing));
    }

    /**
     * Creates a codec which recognizes an object {"key": value} from a codec which recognizes value.
     * This returned codec extracts value from this object, ignoring all other information
     */
    public static <T> Codec<T> singletonKeyCodec(String key, Codec<T> codec) {
        return RecordCodecBuilder.<Optional<T>>create(inst -> inst.group(codec.fieldOf(key).forGetter(Optional::get)).apply(inst, Optional::of))
                .xmap(Optional::get, Optional::of);
    }

    public static void dumpBlockStates() {
        for (int id : ((IIdList) Block.STATE_IDS).multiconnect_ids()) {
            BlockState state = Block.STATE_IDS.get(id);
            assert state != null;
            StringBuilder sb = new StringBuilder().append(id).append(": ").append(Registry.BLOCK.getId(state.getBlock()));
            if (!state.getEntries().isEmpty()) {
                sb.append("[")
                        .append(state.getEntries().entrySet().stream()
                                .sorted(Comparator.comparing(entry -> entry.getKey().getName()))
                                .map(entry -> entry.getKey().getName() + "=" + Util.getValueAsString(entry.getKey(), entry.getValue()))
                                .collect(Collectors.joining(",")))
                        .append("]");
            }
            System.out.println(sb);
        }
    }

    public static DropDownWidget<ConnectionMode> createVersionDropdown(Screen screen, ConnectionMode initialMode) {
        DropDownWidget<ConnectionMode> versionDropDown = new DropDownWidget<>(screen.width - 80, 5, 75, 20, initialMode, mode -> {
            LiteralText text = new LiteralText(mode.getName());
            if (mode.isMulticonnectBeta()) {
                text.append(new LiteralText(" !").formatted(Formatting.RED));
            }
            return text;
        })
        .setCategoryLabelExtractor(mode -> {
            LiteralText text = new LiteralText(mode.getMajorReleaseName());
            if (mode.isMulticonnectBeta()) {
                text.append(new LiteralText(" !").formatted(Formatting.RED));
            }
            return text;
        })
        .setTooltipRenderer((matrices, mode, x, y, isCategory) -> {
            if (mode.isMulticonnectBeta()) {
                String modeName = isCategory ? mode.getMajorReleaseName() : mode.getName();
                screen.renderTooltip(matrices, ImmutableList.of(
                        new TranslatableText("multiconnect.betaWarning.line1", modeName),
                        new TranslatableText("multiconnect.betaWarning.line2", modeName)
                ), x, y);
            }
        });

        // populate the versions
        for (ConnectionMode mode : ConnectionMode.values()) {
            if (mode.isMajorRelease()) {
                DropDownWidget<ConnectionMode>.Category category = versionDropDown.add(mode);
                List<IProtocol> children = mode.getMinorReleases();
                if (children.size() > 1) {
                    for (IProtocol child : children) {
                        category.add((ConnectionMode) child);
                    }
                }
            }
        }

        return versionDropDown;
    }
}
