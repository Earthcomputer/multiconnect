package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

import net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors.CompoundConnector;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors.IBlockConnector;
import net.minecraft.block.Block;

import java.util.*;
import java.util.stream.Collectors;

public class BlockConnections {
    private static final NavigableMap<Integer, Map<Block, IBlockConnector>> connectors = new TreeMap<>();

    public static void registerConnector(int protocol, IBlockConnector connector) {
        for (Block block : connector.getAppliedBlocks()) {
            connectors.computeIfAbsent(protocol, k -> new HashMap<>()).put(block, connector);
        }
    }

    public static BlockConnector buildConnector(int protocol) {
        Map<Block, IBlockConnector> mergedConnectors = new HashMap<>();

        Map<Integer, Map<Block, IBlockConnector>> relevantConnectors = connectors.tailMap(protocol);
        Set<Block> blocks = relevantConnectors.values().stream()
                .flatMap(conn -> conn.keySet().stream())
                .collect(Collectors.toSet());
        for (Block block : blocks) {
            List<IBlockConnector> connectors = relevantConnectors.values().stream()
                    .map(map -> map.get(block))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (connectors.size() == 1) {
                mergedConnectors.put(block, connectors.get(0));
            } else {
                mergedConnectors.put(block, new CompoundConnector(connectors));
            }
        }

        return new BlockConnector(mergedConnectors);
    }
}
