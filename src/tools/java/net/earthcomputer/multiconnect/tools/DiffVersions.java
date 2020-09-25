package net.earthcomputer.multiconnect.tools;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DiffVersions {
    private static final Gson GSON = new Gson();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("From version: ");
        String fromVersion = scanner.nextLine();
        System.out.print("To version: ");
        String toVersion = scanner.nextLine();
        diff(new ArrayDeque<>(), getVersion(fromVersion), getVersion(toVersion));
    }

    private static Version getVersion(String version) {
        URL url = null;
        try {
            url = new URL("https://pokechu22.github.io/Burger/" + version + ".json");
        } catch (MalformedURLException e) {
            System.err.println("Invalid version: " + version);
            System.exit(1);
        }
        try (InputStreamReader reader = new InputStreamReader(url.openConnection().getInputStream(), StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, Version[].class)[0];
        } catch (IOException e) {
            System.err.println("Failed to open connection to " + url);
            System.exit(1);
            return null;
        }
    }

    private static void print(Deque<String> path, String message) {
        System.out.println(String.join(".", path) + ": " + message);
    }

    @SuppressWarnings("unchecked")
    private static void diff(Deque<String> path, Object from, Object to) {
        if (from == null || to == null) {
            if (from != null || to != null) {
                print(path, from + " ==> " + to);
            }
            return;
        }

        if (from.getClass() != to.getClass()) {
            throw new AssertionError();
        }

        if (from.getClass().isPrimitive() || from instanceof Number || from instanceof String) {
            if (!from.equals(to)) {
                print(path, from + " ==> " + to);
            }
        } else if (from instanceof Map) {
            Map<String, Object> fromMap = (Map<String, Object>) from;
            Map<String, Object> toMap = (Map<String, Object>) to;
            fromMap.forEach((key, val) -> {
                if (toMap.containsKey(key)) {
                    path.addLast(key);
                    diff(path, val, toMap.get(key));
                    path.removeLast();
                }
            });
            fromMap.forEach((key, val) -> {
                if (!toMap.containsKey(key)) {
                    print(path, "- " + key);
                }
            });
            toMap.forEach((key, val) -> {
                if (!fromMap.containsKey(key)) {
                    print(path, "+ " + key);
                }
            });
        } else if (from instanceof Collection) {
            throw new UnsupportedOperationException("Collections not supported");
        } else {
            for (Field field : from.getClass().getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
                    field.setAccessible(true);
                    path.addLast(field.getName());
                    try {
                        diff(path, field.get(from), field.get(to));
                    } catch (IllegalAccessException e) {
                        throw new AssertionError();
                    }
                    path.removeLast();
                }
            }
        }
    }

    private static class Version {
        private Blocks blocks;
    }

    private static class Blocks {
        private Map<String, Block> block;
    }

    private static class Block {
        private float hardness;
        private int num_states;
    }

}
