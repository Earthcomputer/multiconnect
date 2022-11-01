package net.earthcomputer.multiconnect.debug;

import com.mojang.datafixers.TypeRewriteRule;
import io.netty.handler.timeout.TimeoutException;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.AbstractProtocol;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.SkipPacketException;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DebugUtils {
    private static final String MULTICONNECT_ISSUES_BASE_URL = "https://github.com/Earthcomputer/multiconnect/issues";
    private static final String MULTICONNECT_ISSUE_URL = MULTICONNECT_ISSUES_BASE_URL + "/%d";
    private static int rareBugIdThatOccurred = 0;
    private static long timeThatRareBugOccurred;
    public static String lastServerBrand = ClientBrandRetriever.VANILLA_NAME;
    public static final boolean UNIT_TEST_MODE = Boolean.getBoolean("multiconnect.unitTestMode");
    public static final boolean SKIP_TRANSLATION = Boolean.getBoolean("multiconnect.skipTranslation");


    private static final Map<EntityDataAccessor<?>, String> ENTITY_DATA_NAMES = new IdentityHashMap<>();
    private static void computeEntityDataNames() {
        Set<Class<?>> entityDataHolders = new HashSet<>();
        for (Field field : EntityType.class.getFields()) {
            if (field.getType() == EntityType.class && Modifier.isStatic(field.getModifiers())) {
                if (field.getGenericType() instanceof ParameterizedType type) {
                    if (type.getActualTypeArguments()[0] instanceof Class<?> entityClass && Entity.class.isAssignableFrom(entityClass)) {
                        for (; entityClass != Object.class; entityClass = entityClass.getSuperclass()) {
                            entityDataHolders.add(entityClass);
                        }
                    }
                }
            }
        }
        for (AbstractProtocol protocol : ProtocolRegistry.all()) {
            entityDataHolders.add(protocol.getClass());
        }

        for (Class<?> entityDataHolder : entityDataHolders) {
            for (Field field : entityDataHolder.getDeclaredFields()) {
                if (field.getType() == EntityDataAccessor.class && Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    EntityDataAccessor<?> entityData;
                    try {
                        entityData = (EntityDataAccessor<?>) field.get(null);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                    ENTITY_DATA_NAMES.put(entityData, entityDataHolder.getSimpleName() + "::" + field.getName());
                }
            }
        }
    }

    public static String getEntityDataName(EntityDataAccessor<?> data) {
        if (ENTITY_DATA_NAMES.isEmpty()) {
            computeEntityDataNames();
        }
        String name = ENTITY_DATA_NAMES.get(data);
        return name == null ? "unknown" : name;
    }

    public static String getAllEntityData(Entity entity) {
        List<SynchedEntityData.DataItem<?>> allEntries = entity.getEntityData().getAll();
        if (allEntries == null || allEntries.isEmpty()) {
            return "<no entries>";
        }

        return allEntries.stream()
                .sorted(Comparator.comparingInt(entry -> entry.getAccessor().getId()))
                .map(entry -> entry.getAccessor().getId() + ": " + getEntityDataName(entry.getAccessor()) + " = " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    public static void reportRareBug(int bugId) {
        rareBugIdThatOccurred = bugId;
        timeThatRareBugOccurred = System.nanoTime();

        Minecraft mc = Minecraft.getInstance();
        if (!mc.isSameThread()) {
            mc.tell(() -> reportRareBug(bugId));
            return;
        }

        String url = MULTICONNECT_ISSUE_URL.formatted(rareBugIdThatOccurred);
        mc.gui.getChat().addMessage(Component.translatable("multiconnect.rareBug", Component.translatable("multiconnect.rareBug.link")
                        .withStyle(style -> style.withUnderlined(true)
                                .withColor(ChatFormatting.BLUE)
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(url)))
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))))
            .withStyle(ChatFormatting.YELLOW));
    }

    public static boolean wasRareBugReportedRecently() {
        return rareBugIdThatOccurred != 0 && (System.nanoTime() - timeThatRareBugOccurred) < 10_000_000_000L;
    }

    private static Component getRareBugText(int line) {
        String url = MULTICONNECT_ISSUE_URL.formatted(rareBugIdThatOccurred);
        return Component.translatable("multiconnect.rareBug", Component.translatable("multiconnect.rareBug.link")
                .withStyle(style -> style.withUnderlined(true)
                        .withColor(ChatFormatting.BLUE)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(url)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))));
    }

    public static Screen createRareBugScreen(Screen parentScreen) {
        URL url;
        try {
            url = new URL(MULTICONNECT_ISSUE_URL.formatted(rareBugIdThatOccurred));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        rareBugIdThatOccurred = 0;
        return new ConfirmScreen(result -> {
            if (result) {
                Util.getPlatform().openUrl(url);
            }
            Minecraft.getInstance().setScreen(parentScreen);
        }, parentScreen.getTitle(), Component.translatable("multiconnect.rareBug.screen"));
    }

    @ThreadSafe
    public static boolean isUnexpectedDisconnect(Throwable t) {
        return !(t instanceof SkipPacketException) && !(t instanceof TimeoutException);
    }

    public static void onDebugKey() {
    }

    public static String dfuToString(@Nullable Object dfuType) {
        if (dfuType == null) {
            return "null";
        }
        if (dfuType instanceof Iterable<?> collection) {
            StringBuilder sb = new StringBuilder("[");
            boolean appended = false;
            for (Object o : collection) {
                if (appended) {
                    sb.append(",\n");
                } else {
                    sb.append("\n");
                }
                appended = true;
                sb.append(dfuToString(o));
            }
            return sb.toString().replace("\n", "  \n") + "\n]";
        }

        if (dfuType instanceof TypeRewriteRule.Nop) {
            return "Nop";
        }
        if (dfuType instanceof TypeRewriteRule.Seq) {
            return "Seq" + dfuToString(getField(dfuType, "rules"));
        }
        if (dfuType instanceof TypeRewriteRule.OrElse) {
            return "OrElse[" + dfuToString(getField(dfuType, "first")) + ", " + dfuToString(getField(dfuType, "second")) + "]";
        }
        if (dfuType instanceof TypeRewriteRule.All) {
            return "All[" + dfuToString(getField(dfuType, "rule")) + "]";
        }
        if (dfuType instanceof TypeRewriteRule.One) {
            return "One[" + dfuToString(getField(dfuType, "rule")) + "]";
        }
        if (dfuType instanceof TypeRewriteRule.CheckOnce) {
            return "CheckOnce[" + dfuToString(getField(dfuType, "rule")) + "]";
        }
        if (dfuType instanceof TypeRewriteRule.Everywhere) {
            return "Everywhere[" + dfuToString(getField(dfuType, "rule")) + "]";
        }
        if (dfuType instanceof TypeRewriteRule.IfSame<?>) {
            return "IfSame[" + dfuToString(getField(dfuType, "targetType")) + ", " + dfuToString(getField(dfuType, "value")) + "]";
        }

        return dfuType.toString();
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object instance, String name) {
        for (Class<?> clazz = instance.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return (T) field.get(instance);
            } catch (NoSuchFieldException ignored) {
            } catch (ReflectiveOperationException e) {
                Util.throwAsRuntime(e);
            }
        }
        throw new IllegalArgumentException("No such field " + name);
    }
}
