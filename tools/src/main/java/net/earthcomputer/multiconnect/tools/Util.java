package net.earthcomputer.multiconnect.tools;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class Util {
    private Util() {}

    @Nullable
    public static Integer tryParse(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String substringBefore(String s, char c) {
        int i = s.indexOf(c);
        if (i == -1) {
            return s;
        } else {
            return s.substring(0, i);
        }
    }

    @Nullable
    public static <T> T getOrNull(List<T> list, int index) {
        if (index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    public static int compareVersions(String a, String b) {
        String[] partsA = a.split(" ");
        String[] partsB = b.split(" ");
        for (int i = 0, e = Math.min(partsA.length, partsB.length); i < e; i++) {
            Integer ia = tryParse(partsA[i]);
            Integer ib = tryParse(partsB[i]);
            if (ia == null) {
                if (ib != null) {
                    return 1;
                }
            } else if (ib == null) {
                return -1;
            } else {
                int cmp = ia.compareTo(ib);
                if (cmp != 0) {
                    return cmp;
                }
            }
        }
        return Integer.compare(partsA.length, partsB.length);
    }

    @Contract("null -> null")
    public static String normalizeIdentifier(@Nullable String s) {
        if (s == null) {
            return null;
        }
        if (s.contains(":")) {
            return s;
        } else {
            return "minecraft:" + s;
        }
    }
}
