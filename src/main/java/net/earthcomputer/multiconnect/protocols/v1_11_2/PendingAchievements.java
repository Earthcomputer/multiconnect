package net.earthcomputer.multiconnect.protocols.v1_11_2;

import net.minecraft.advancement.Advancement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PendingAchievements {

    private static final Set<Advancement> achievementsToAdd = new HashSet<>();
    private static final Set<Advancement> achievementsToRemove = new HashSet<>();

    private final List<Advancement> toAdd;
    private final List<Advancement> toRemove;

    private PendingAchievements(List<Advancement> toAdd, List<Advancement> toRemove) {
        this.toAdd = toAdd;
        this.toRemove = toRemove;
    }

    public static synchronized void giveAchievement(Advancement achievement) {
        achievementsToAdd.add(achievement);
        achievementsToRemove.remove(achievement);
    }

    public static synchronized void takeAchievement(Advancement achievement) {
        achievementsToAdd.remove(achievement);
        achievementsToRemove.add(achievement);
    }

    public static synchronized PendingAchievements poll() {
        PendingAchievements pending = new PendingAchievements(new ArrayList<>(achievementsToAdd), new ArrayList<>(achievementsToRemove));
        achievementsToAdd.clear();
        achievementsToRemove.clear();
        return pending;
    }

    public List<Advancement> getToAdd() {
        return toAdd;
    }

    public List<Advancement> getToRemove() {
        return toRemove;
    }
}
