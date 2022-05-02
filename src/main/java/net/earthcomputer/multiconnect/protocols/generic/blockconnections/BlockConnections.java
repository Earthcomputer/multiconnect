package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.protocols.generic.Key;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors.CompoundConnector;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors.IBlockConnector;
import net.minecraft.block.Block;
import net.minecraft.util.EightWayDirection;

import java.util.*;
import java.util.stream.Collectors;

public class BlockConnections {
    public static final Key<EnumMap<EightWayDirection, IntSet>> BLOCKS_NEEDING_UPDATE_KEY = Key.create("blocksNeedingUpdate", () -> new EnumMap<>(EightWayDirection.class));

    private static final NavigableMap<Integer, Map<Block, IBlockConnector>> connectors = new TreeMap<>();

    public static void registerConnector(int protocol, IBlockConnector connector) {
        for (Block block : connector.getAppliedBlocks()) {
            connectors.computeIfAbsent(protocol, k -> new HashMap<>()).put(block, connector);
        }
    }

    @ThreadSafe
    public static synchronized BlockConnector buildConnector(int protocol) {
        var mergedConnectors = new HashMap<Block, IBlockConnector>();

        var relevantConnectors = connectors.tailMap(protocol);
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
