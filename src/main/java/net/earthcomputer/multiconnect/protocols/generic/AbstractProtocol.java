package net.earthcomputer.multiconnect.protocols.generic;

import net.earthcomputer.multiconnect.mixin.bridge.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

public abstract class AbstractProtocol {
    private static final List<Block> collisionBoxesToRevert = new ArrayList<>();

    public void setup() {
        revertCollisionBoxes();
        AssetDownloader.reloadLanguages();
        markChangedCollisionBoxes();
        ((MinecraftAccessor) Minecraft.getInstance()).getSearchRegistry().onResourceManagerReload(Minecraft.getInstance().getResourceManager());
    }

    public void disable() {
    }

    protected void markChangedCollisionBoxes() {
    }

    private void revertCollisionBoxes() {
        for (Block block : collisionBoxesToRevert) {
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                state.initCache();
            }
        }
        collisionBoxesToRevert.clear();
    }

    protected void markCollisionBoxChanged(Block block) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            state.initCache();
        }
        collisionBoxesToRevert.add(block);
    }

    public float getBlockDestroySpeed(BlockState state, float destroySpeed) {
        return destroySpeed;
    }

    public float getBlockExplosionResistance(Block block, float resistance) {
        return resistance;
    }
}
