package net.earthcomputer.multiconnect.impl;

import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class AligningFormatter {
    private int nextNodeId = 0;
    private final Int2ObjectMap<Node> nodes = new Int2ObjectOpenHashMap<>();
    private final List<IntList> rows = new ArrayList<>();
    private boolean rowsDirty = false;

    public void insertRow(int index) {
        if (index != rows.size()) {
            rowsDirty = true;
        }
        rows.add(index, new IntArrayList());
    }

    public int addRow() {
        int index = rows.size();
        insertRow(index);
        return index;
    }

    public int rowCount() {
        return rows.size();
    }

    public int rowSize(int row) {
        return rows.get(row).size();
    }

    public int insert(int row, int index, String text) {
        int nodeId = nextNodeId++;
        rows.get(row).add(index, nodeId);
        nodes.put(nodeId, new Node(nodeId, text, row));
        return nodeId;
    }

    public int add(int row, String text) {
        return insert(row, rowSize(row), text);
    }

    public int add(String text) {
        return add(rows.size() - 1, text);
    }

    public void leftAlign(int from, int to) {
        Node fromNode = nodes.get(from);
        Node toNode = nodes.get(to);
        if (fromNode.leftAlignedTo != -1) {
            if (toNode.leftAlignedTo == -1) {
                leftAlign(to, from);
                return;
            }
            int tmp = fromNode.leftAlignedTo;
            fromNode.leftAlignedTo = toNode.leftAlignedTo;
            toNode.leftAlignedTo = tmp;
        } else {
            // ensure we include all aligned nodes in a cycle
            if (toNode.leftAlignedTo == -1) {
                toNode.leftAlignedTo = from;
                fromNode.leftAlignedTo = to;
            } else {
                fromNode.leftAlignedTo = toNode.leftAlignedTo;
                toNode.leftAlignedTo = from;
            }
        }
        checkRowCycle(fromNode, node -> node.leftAlignedTo);
    }

    public void rightAlign(int from, int to) {
        Node fromNode = nodes.get(from);
        Node toNode = nodes.get(to);
        if (fromNode.rightAlignedTo != -1) {
            if (toNode.rightAlignedTo == -1) {
                rightAlign(to, from);
                return;
            }
            int tmp = fromNode.rightAlignedTo;
            fromNode.rightAlignedTo = toNode.rightAlignedTo;
            toNode.rightAlignedTo = tmp;
        } else {
            // ensure we include all aligned nodes in a cycle
            if (toNode.rightAlignedTo == -1) {
                toNode.rightAlignedTo = from;
                fromNode.rightAlignedTo = to;
            } else {
                fromNode.rightAlignedTo = toNode.rightAlignedTo;
                toNode.rightAlignedTo = from;
            }
        }
        checkRowCycle(fromNode, node -> node.rightAlignedTo);
    }

    private void checkRowCycle(Node node, ToIntFunction<Node> alignedToExtractor) {
        if (rowsDirty) {
            rowsDirty = false;
            for (int rowId = 0; rowId < rows.size(); rowId++) {
                IntList row = rows.get(rowId);
                IntIterator itr = row.iterator();
                while (itr.hasNext()) {
                    int nodeId = itr.nextInt();
                    nodes.get(nodeId).row = rowId;
                }
            }
        }

        IntSet encounteredRows = new IntOpenHashSet();
        int firstNodeId = node.id;
        int nodeId;
        do {
            nodeId = alignedToExtractor.applyAsInt(node);
            node = nodes.get(nodeId);
            if (!encounteredRows.add(node.row)) {
                StringBuilder sb = new StringBuilder("Detected row cycle");
                nodeId = firstNodeId;
                node = nodes.get(nodeId);
                do {
                    sb.append(" -> ").append(node.value).append(" (").append(nodeId).append(", ").append(node.row).append(")");
                    nodeId = alignedToExtractor.applyAsInt(node);
                    node = nodes.get(nodeId);
                } while (nodeId != firstNodeId);
                throw new IllegalStateException(sb.toString());
            }
        } while (nodeId != firstNodeId);
    }

    public void hExpand(int node) {
        nodes.get(node).hExpand = true;
    }

    public void vAttach(int node, int targetNode, char attachChar) {
        Node nodeObj = nodes.get(node);
        nodeObj.vAttachingTo = targetNode;
        nodeObj.vAttach = attachChar;
    }

    public List<String> getLines() {
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }

        final class Edge implements Comparable<Edge> {
            final List<Node> nodes = new ArrayList<>();
            Edge rowPredecessor;
            Edge rowSuccessor;
            int minLength;
            int depth;

            @Override
            public int compareTo(Edge o) {
                return Integer.compare(depth, o.depth);
            }
        }

        Int2ObjectMap<Edge> nodeToEdge = new Int2ObjectOpenHashMap<>();
        List<List<Edge>> edgeRows = new ArrayList<>(rows.size());
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            IntList row = rows.get(rowIndex);
            List<Edge> edgeRow = new ArrayList<>();
            edgeRows.add(edgeRow);
            if (row.isEmpty()) {
                continue;
            }
            Edge edge = new Edge();
            for (int col = 0; col < row.size(); col++) {
                int nodeId = row.getInt(col);
                Node node = nodes.get(nodeId);
                node.row = rowIndex;
                if (node.leftAlignedTo != -1 && (!edge.nodes.isEmpty() || (col != 0 && nodes.get(row.getInt(col - 1)).rightAlignedTo != -1))) {
                    edgeRow.add(edge);
                    Edge nextEdge = new Edge();
                    nextEdge.rowPredecessor = edge;
                    edge.rowSuccessor = nextEdge;
                    edge = nextEdge;
                }
                nodeToEdge.put(nodeId, edge);
                edge.nodes.add(node);
                edge.minLength += node.value.length();
                if (node.rightAlignedTo != -1) {
                    edgeRow.add(edge);
                    Edge nextEdge = new Edge();
                    nextEdge.rowPredecessor = edge;
                    edge.rowSuccessor = nextEdge;
                    edge = nextEdge;
                }
            }
            edgeRow.add(edge);
        }

        List<Edge> edgesToCheck = new ArrayList<>();
        for (IntList row : rows) {
            if (!row.isEmpty()) {
                int firstNodeId = row.getInt(0);
                Node firstNode = nodes.get(firstNodeId);
                if (firstNode.leftAlignedTo == -1) {
                    edgesToCheck.add(nodeToEdge.get(firstNodeId));
                } else {
                    int nodeId = firstNodeId;
                    Node node = firstNode;
                    boolean isStartingEdge = true;
                    do {
                        Edge edge = nodeToEdge.get(nodeId);
                        if (edge.rowPredecessor != null) {
                            isStartingEdge = false;
                            break;
                        }
                        nodeId = node.leftAlignedTo;
                        node = nodes.get(nodeId);
                    } while (nodeId != firstNodeId);
                    if (isStartingEdge) {
                        edgesToCheck.add(nodeToEdge.get(firstNodeId));
                    }
                }
            }
        }

        if (edgesToCheck.isEmpty()) {
            throw new IllegalStateException("Cyclic references in AligningFormatter");
        }

        while (!edgesToCheck.isEmpty()) {
            Edge edge = edgesToCheck.remove(0);
            int nextDepth = edge.depth + edge.minLength;
            if (!edge.nodes.isEmpty()) {
                if (edge.nodes.get(0).leftAlignedTo != -1) {
                    Node node = edge.nodes.get(0);
                    int firstNodeId = node.id;
                    int nodeId = firstNodeId;
                    int maxDepth = -1;
                    do {
                        int depth = nodeToEdge.get(nodeId).depth;
                        if (depth > maxDepth) {
                            maxDepth = depth;
                        }
                        nodeId = node.leftAlignedTo;
                        node = nodes.get(nodeId);
                    } while (nodeId != firstNodeId);
                    do {
                        Edge otherEdge = nodeToEdge.get(nodeId);
                        if (maxDepth != otherEdge.depth) {
                            int index = binaryIndexOf(edgesToCheck, otherEdge);
                            if (index != -1) edgesToCheck.remove(index);
                            otherEdge.depth = maxDepth;
                            if (otherEdge != edge) {
                                index = Collections.binarySearch(edgesToCheck, otherEdge);
                                if (index < 0) index = -index - 1;
                                edgesToCheck.add(index, otherEdge);
                            }
                        }
                        nodeId = node.leftAlignedTo;
                        node = nodes.get(nodeId);
                    } while (nodeId != firstNodeId);
                }
                nextDepth = edge.depth + edge.minLength;
                if (Util.getLast(edge.nodes).rightAlignedTo != -1) {
                    Node node = Util.getLast(edge.nodes);
                    int firstNodeId = node.id;
                    int nodeId = firstNodeId;
                    int maxNextDepth = -1;
                    do {
                        Edge otherEdge = nodeToEdge.get(nodeId);
                        if (maxNextDepth < otherEdge.depth + otherEdge.minLength) {
                            maxNextDepth = otherEdge.depth + otherEdge.minLength;
                        }
                        nodeId = node.rightAlignedTo;
                        node = nodes.get(nodeId);
                    } while (nodeId != firstNodeId);
                    do {
                        Edge otherEdge = nodeToEdge.get(nodeId);
                        if (otherEdge != edge && otherEdge.rowSuccessor.depth != maxNextDepth && binaryIndexOf(edgesToCheck, otherEdge) == -1) {
                            int index = Collections.binarySearch(edgesToCheck, otherEdge);
                            if (index < 0) index = -index - 1;
                            edgesToCheck.add(index, otherEdge);
                        }
                        nodeId = node.rightAlignedTo;
                        node = nodes.get(nodeId);
                    } while (nodeId != firstNodeId);
                    nextDepth = maxNextDepth;
                }
            }

            if (edge.rowSuccessor != null) {
                edge.rowSuccessor.depth = nextDepth;
                if (binaryIndexOf(edgesToCheck, edge.rowSuccessor) == -1) {
                    int index = Collections.binarySearch(edgesToCheck, edge.rowSuccessor);
                    if (index < 0) index = -index - 1;
                    edgesToCheck.add(index, edge.rowSuccessor);
                }
            }
        }

        List<StringBuilder> lines = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            lines.add(new StringBuilder());
        }
        List<VAttach> vAttaches = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < edgeRows.size(); rowIndex++) {
            List<Edge> edgeRow = edgeRows.get(rowIndex);
            StringBuilder sb = lines.get(rowIndex);
            for (int edgeIndex = 0; edgeIndex < edgeRow.size(); edgeIndex++) {
                Edge edge = edgeRow.get(edgeIndex);
                while (sb.length() < edge.depth) {
                    sb.append(' ');
                }
                if (!edge.nodes.isEmpty()) {
                    boolean leftAligned = edge.nodes.get(0).leftAlignedTo != -1;
                    boolean rightAligned = Util.getLast(edge.nodes).rightAlignedTo != -1;
                    if (rightAligned) {
                        if (leftAligned) {
                            int spacesNeeded = edgeRow.get(edgeIndex + 1).depth - edge.depth - edge.minLength;
                            for (int nodeIndex = 0; nodeIndex < edge.nodes.size(); nodeIndex++) {
                                Node node = edge.nodes.get(nodeIndex);
                                char leftChar = node.value.isEmpty() || !node.hExpand ? ' ' : node.value.charAt(0);
                                int leftPaddingNeeded = ((spacesNeeded + nodeIndex) / edge.nodes.size()) / 2;
                                for (int i = 0; i < leftPaddingNeeded; i++) {
                                    sb.append(leftChar);
                                }
                                addVAttach(vAttaches, sb, node);
                                sb.append(node.value);
                                char rightChar = node.value.isEmpty() || !node.hExpand ? ' ' : node.value.charAt(node.value.length() - 1);
                                int rightPaddingNeeded = (((spacesNeeded + nodeIndex) / edge.nodes.size()) + 1) / 2;
                                for (int i = 0; i < rightPaddingNeeded; i++) {
                                    sb.append(rightChar);
                                }
                            }
                        } else {
                            for (int i = 0, e = edgeRow.get(edgeIndex + 1).depth - edge.depth - edge.minLength; i < e; i++) {
                                sb.append(' ');
                            }
                            for (Node node : edge.nodes) {
                                addVAttach(vAttaches, sb, node);
                                sb.append(node.value);
                            }
                        }
                    } else {
                        for (Node node : edge.nodes) {
                            addVAttach(vAttaches, sb, node);
                            sb.append(node.value);
                        }
                    }
                }
            }
        }

        for (VAttach vAttach : vAttaches) {
            for (int row = vAttach.fromRow + 1; row < vAttach.toRow; row++) {
                StringBuilder sb = lines.get(row);
                while (sb.length() <= vAttach.hIndex) {
                    sb.append(' ');
                }
                if (sb.charAt(vAttach.hIndex) == ' ') {
                    sb.setCharAt(vAttach.hIndex, vAttach.c);
                }
            }
        }

        return lines.stream().map(StringBuilder::toString).collect(Collectors.toList());
    }

    public void dumpUnformatted() {
        for (IntList row : rows) {
            StringBuilder line = new StringBuilder();
            IntIterator itr = row.iterator();
            while (itr.hasNext()) {
                if (line.length() != 0) {
                    line.append(", ");
                }
                int nodeId = itr.nextInt();
                Node node = nodes.get(nodeId);
                if (node.leftAlignedTo != -1) {
                    line.append("[").append(node.leftAlignedTo).append("]");
                }
                if (node.hExpand) {
                    line.append("<-");
                }
                line.append("\"").append(node.value).append("\"");
                if (node.hExpand) {
                    line.append("->");
                }
                line.append("(").append(nodeId).append(")");
                if (node.vAttach != 0) {
                    line.append("^");
                }
                if (node.rightAlignedTo != -1) {
                    line.append("{").append(node.rightAlignedTo).append("}");
                }
            }
            System.out.println(line);
        }
    }

    private void addVAttach(List<VAttach> vAttaches, StringBuilder sb, Node node) {
        if (node.vAttach != 0) {
            vAttaches.add(new VAttach(
                    sb.length() + node.value.length() / 2,
                    Math.min(node.row, nodes.get(node.vAttachingTo).row),
                    Math.max(node.row, nodes.get(node.vAttachingTo).row),
                    node.vAttach
            ));
        }
    }

    private static <T extends Comparable<T>> int binaryIndexOf(List<T> list, T element) {
        int index = Collections.binarySearch(list, element);
        if (index < 0) return -1;
        if (list.get(index).equals(element)) {
            return index;
        }
        for (int i = index - 1; i >= 0 && list.get(i).compareTo(element) == 0; i--) {
            if (list.get(i).equals(element)) {
                return i;
            }
        }
        for (int i = index + 1; i < list.size() && list.get(i).compareTo(element) == 0; i++) {
            if (list.get(i).equals(element)) {
                return i;
            }
        }
        return -1;
    }

    private static final class Node {
        final int id;
        final String value;
        int leftAlignedTo = -1;
        int rightAlignedTo = -1;
        boolean hExpand;
        int vAttachingTo;
        char vAttach = 0;
        int row;

        Node(int id, String value, int row) {
            this.id = id;
            this.value = value;
            this.row = row;
        }
    }

    private static final class VAttach {
        int hIndex;
        int fromRow;
        int toRow;
        char c;

        public VAttach(int hIndex, int fromRow, int toRow, char c) {
            this.hIndex = hIndex;
            this.fromRow = fromRow;
            this.toRow = toRow;
            this.c = c;
        }
    }
}
