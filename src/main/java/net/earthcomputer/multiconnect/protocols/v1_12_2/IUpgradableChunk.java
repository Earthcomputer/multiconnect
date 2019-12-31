package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.minecraft.world.chunk.UpgradeData;

public interface IUpgradableChunk {

    UpgradeData multiconnect_getClientUpgradeData();

    void multiconnect_setClientUpgradeData(UpgradeData upgradeData);

    void multiconnect_onNeighborLoaded();

}
