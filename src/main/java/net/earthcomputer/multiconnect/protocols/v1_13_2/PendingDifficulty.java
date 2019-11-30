package net.earthcomputer.multiconnect.protocols.v1_13_2;

import net.minecraft.world.Difficulty;

public class PendingDifficulty {

    private static Difficulty pendingDifficulty;

    public static Difficulty getPendingDifficulty() {
        return pendingDifficulty;
    }

    public static void setPendingDifficulty(Difficulty pendingDifficulty) {
        PendingDifficulty.pendingDifficulty = pendingDifficulty;
    }
}
