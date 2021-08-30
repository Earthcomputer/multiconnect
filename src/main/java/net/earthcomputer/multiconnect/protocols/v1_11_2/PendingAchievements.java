package net.earthcomputer.multiconnect.protocols.v1_11_2;

import net.earthcomputer.multiconnect.protocols.generic.Key;
import net.minecraft.advancement.Advancement;

import java.util.List;

public record PendingAchievements(List<Advancement> toAdd, List<Advancement> toRemove) {
    public static final Key<PendingAchievements> KEY = Key.create("pendingAchievements");
}
