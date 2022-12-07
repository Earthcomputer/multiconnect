package net.earthcomputer.multiconnect.debug;

import com.mojang.datafixers.TypeRewriteRule;
import io.netty.handler.timeout.TimeoutException;
import net.earthcomputer.multiconnect.api.ThreadSafe;
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
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;

public class DebugUtils {
    private static final String MULTICONNECT_ISSUES_BASE_URL = "https://github.com/Earthcomputer/multiconnect/issues";
    private static final String MULTICONNECT_ISSUE_URL = MULTICONNECT_ISSUES_BASE_URL + "/%d";
    private static int rareBugIdThatOccurred = 0;
    private static long timeThatRareBugOccurred;
    public static String lastServerBrand = ClientBrandRetriever.VANILLA_NAME;
    public static final boolean UNIT_TEST_MODE = Boolean.getBoolean("multiconnect.unitTestMode");

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
