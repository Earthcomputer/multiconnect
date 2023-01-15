package net.earthcomputer.multiconnect.protocols.generic;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.api.ProtocolBehavior;
import net.earthcomputer.multiconnect.mixin.bridge.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ProtocolBehaviorSet {
    private static final Set<Block> collisionBoxesToRevert = new HashSet<>();
    private final List<ProtocolBehavior> behaviors;

    public ProtocolBehaviorSet(List<ProtocolBehavior> behaviors) {
        this.behaviors = behaviors;
    }

    public void setup() {
        revertCollisionBoxes();
        AssetDownloader.reloadLanguages();
        markChangedCollisionBoxes();
        ((MinecraftAccessor) Minecraft.getInstance()).getSearchRegistry().onResourceManagerReload(Minecraft.getInstance().getResourceManager());
        for (ProtocolBehavior behavior : behaviors) {
            behavior.onSetup();
        }
    }

    public void disable() {
        for (int i = behaviors.size() - 1; i >= 0; i--) {
            behaviors.get(i).onDisable();
        }
    }

    protected void markChangedCollisionBoxes() {
        for (ProtocolBehavior behavior : behaviors) {
            for (Block block : behavior.getBlocksWithChangedCollision()) {
                markCollisionBoxChanged(block);
            }
        }
    }

    private void revertCollisionBoxes() {
        for (Block block : collisionBoxesToRevert) {
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                state.initCache();
            }
        }
        collisionBoxesToRevert.clear();
    }

    private void markCollisionBoxChanged(Block block) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            state.initCache();
        }
        collisionBoxesToRevert.add(block);
    }

    public float getBlockDestroySpeed(BlockState state, float destroySpeed) {
        for (ProtocolBehavior behavior : behaviors) {
            Float result = behavior.getDestroySpeed(state, destroySpeed);
            if (result != null) {
                return result;
            }
        }
        return destroySpeed;
    }

    public float getBlockExplosionResistance(Block block, float resistance) {
        for (ProtocolBehavior behavior : behaviors) {
            Float result = behavior.getExplosionResistance(block, resistance);
            if (result != null) {
                return result;
            }
        }
        return resistance;
    }

    public void register112Commands(CommandBuildContext context, CommandDispatcher<SharedSuggestionProvider> dispatcher, @Nullable Set<String> serverCommands) {
        record Args(
            CommandBuildContext context,
            CommandDispatcher<SharedSuggestionProvider> dispatcher,
            @Nullable Set<String> serverCommands
        ) implements ProtocolBehavior.CommandRegistrationArgs {}

        Args args = new Args(context, dispatcher, serverCommands);
        for (int i = behaviors.size() - 1; i >= 0; i--) {
            behaviors.get(i).onCommandRegistration(args);
        }
    }
}
