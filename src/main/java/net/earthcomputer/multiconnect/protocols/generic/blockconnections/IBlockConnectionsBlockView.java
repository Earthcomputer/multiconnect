package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface IBlockConnectionsBlockView {
    Logger LOGGER = LogUtils.getLogger();
    Set<Block> WARNED_NPE_BLOCKS = ConcurrentHashMap.newKeySet();

    BlockState getBlockState(BlockPos pos);
    void setBlockState(BlockPos pos, BlockState state);

    int getMinY();
    int getMaxY();

    static <T> T withNullLevel(Block block, T def, Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (NullPointerException e) {
            if (WARNED_NPE_BLOCKS.add(block)) {
                LOGGER.warn("Block {} threw NPE with null level", Registry.BLOCK.getKey(block));
            }
            return def;
        }
    }
}
