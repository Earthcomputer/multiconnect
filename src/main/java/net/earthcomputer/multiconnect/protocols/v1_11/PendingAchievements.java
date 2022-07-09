package net.earthcomputer.multiconnect.protocols.v1_11;

import net.earthcomputer.multiconnect.protocols.generic.Key;
import net.minecraft.advancements.Advancement;

import java.util.List;

public record PendingAchievements(List<Advancement> toAdd, List<Advancement> toRemove) {
    public static final Key<PendingAchievements> KEY = Key.create("pendingAchievements");
}
