package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public interface IBlockConnectionsBlockView {
    Logger LOGGER = LogManager.getLogger("multiconnect");
    Set<Block> WARNED_NPE_BLOCKS = new HashSet<>();

    BlockState getBlockState(BlockPos pos);
    boolean setBlockState(BlockPos pos, BlockState state);

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
