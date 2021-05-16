package net.earthcomputer.multiconnect.protocols.v1_11_2;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.util.Identifier;

import java.util.*;

public class AchievementManager {

    public static final String REQUIREMENT = "requirement";
    private static final int VISIBILITY_DEPTH = 3;

    private static final Set<Advancement> lastVisibleAchievements = new HashSet<>();
    private static final Set<Advancement> visibleAchievements = new HashSet<>();
    private static final Set<Advancement> lastObtainedAchievements = new HashSet<>();
    private static final Set<Advancement> obtainedAchievements = new HashSet<>();
    private static boolean firstUpdate = true;

    public static void setToDefault() {
        lastVisibleAchievements.clear();
        visibleAchievements.clear();
        lastObtainedAchievements.clear();
        obtainedAchievements.clear();
        visibleAchievements.add(Achievements_1_11_2.OPEN_INVENTORY);
        for (int i = 1; i < VISIBILITY_DEPTH; i++) {
            incrementVisible();
        }
        synchronize(true);
        firstUpdate = true;
    }

    public static void update(List<Advancement> addedAchievements, List<Advancement> removedAchievements) {
        obtainedAchievements.removeAll(removedAchievements);
        obtainedAchievements.addAll(addedAchievements);
        visibleAchievements.clear();
        visibleAchievements.addAll(obtainedAchievements);

        if (obtainedAchievements.isEmpty()) {
            visibleAchievements.add(Achievements_1_11_2.OPEN_INVENTORY);
        } else {
            incrementVisible();
        }
        for (int i = 1; i < VISIBILITY_DEPTH; i++) {
            incrementVisible();
        }

        if (firstUpdate) {
            lastObtainedAchievements.clear();
            lastVisibleAchievements.clear();
            synchronize(true);
            firstUpdate = false;
        } else {
            synchronize(false);
        }
    }

    private static void incrementVisible() {
        Set<Advancement> vis = new HashSet<>(visibleAchievements);
        for (Advancement achievement : Achievements_1_11_2.ACHIEVEMENTS.values()) {
            if (vis.contains(achievement.getParent())) {
                visibleAchievements.add(achievement);
            }
        }
    }

    private static void synchronize(boolean clearAll) {
        Set<Advancement> toEarn = new HashSet<>();
        Set<Identifier> toRemove = new HashSet<>();
        var progress = new HashMap<Identifier, AdvancementProgress>();

        for (Advancement achievement : visibleAchievements) {
            if (!lastVisibleAchievements.contains(achievement)) {
                toEarn.add(achievement);
            }
            if (!lastVisibleAchievements.contains(achievement) || obtainedAchievements.contains(achievement) != lastObtainedAchievements.contains(achievement)) {
                AdvancementProgress p = new AdvancementProgress();
                p.init(achievement.getCriteria(), achievement.getRequirements());
                if (obtainedAchievements.contains(achievement)) {
                    p.obtain(REQUIREMENT);
                }
                progress.put(achievement.getId(), p);
            }
        }

        for (Advancement achievement : lastVisibleAchievements) {
            if (!visibleAchievements.contains(achievement)) {
                toRemove.add(achievement.getId());
            }
        }

        lastVisibleAchievements.clear();
        lastVisibleAchievements.addAll(visibleAchievements);
        lastObtainedAchievements.clear();
        lastObtainedAchievements.addAll(obtainedAchievements);

        if (clearAll || !toEarn.isEmpty() || !toRemove.isEmpty() || !progress.isEmpty()) {
            assert MinecraftClient.getInstance().getNetworkHandler() != null;
            MinecraftClient.getInstance().getNetworkHandler().onAdvancements(new AdvancementUpdateS2CPacket(clearAll,
                    toEarn, toRemove, progress));
        }
    }

}
