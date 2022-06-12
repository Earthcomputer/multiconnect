package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

import com.mojang.logging.LogUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public interface IBlockConnectionsBlockView {
    Logger LOGGER = LogUtils.getLogger();
    Set<Block> WARNED_NPE_BLOCKS = ConcurrentHashMap.newKeySet();

    BlockState getBlockState(BlockPos pos);
    void setBlockState(BlockPos pos, BlockState state);

    int getMinY();
    int getMaxY();

    static <T> T withNullWorld(Block block, T def, Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (NullPointerException e) {
            if (WARNED_NPE_BLOCKS.add(block)) {
                LOGGER.warn("Block {} threw NPE with null world", Registry.BLOCK.getId(block));
            }
            return def;
        }
    }
}
