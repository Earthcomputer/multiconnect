package net.earthcomputer.multiconnect.protocols;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.earthcomputer.multiconnect.api.IProtocol;
import net.earthcomputer.multiconnect.api.ProtocolBehavior;
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.impl.IProtocolExt;
import net.earthcomputer.multiconnect.protocols.generic.ProtocolBehaviorSet;
import net.minecraft.SharedConstants;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ProtocolRegistry {
    private static final Int2ObjectOpenHashMap<ProtocolEntry> protocols = new Int2ObjectOpenHashMap<>();
    private static final List<ProtocolEntry> sortedProtocols = new ArrayList<>();

    static {
        // make sure ConnectionMode class is initialized
        //noinspection ResultOfMethodCallIgnored
        ConnectionMode.values();
    }

    public static boolean isSupported(int version) {
        return protocols.containsKey(version);
    }

    public static boolean isSupportedName(String name) {
        for (ProtocolEntry protocol : sortedProtocols) {
            if (name.equals(protocol.protocol.getName())) {
                return true;
            }
        }
        return false;
    }

    public static List<IProtocolExt> getProtocols() {
        return sortedProtocols.stream().map(ProtocolEntry::protocol).toList();
    }

    public static IProtocolExt get(int version) {
        ProtocolEntry entry = protocols.get(version);
        if (entry == null) {
            throw new IllegalArgumentException("No protocol registered for version " + version);
        }
        return protocols.get(version).protocol;
    }

    public static ProtocolBehaviorSet getBehaviorSet(int version) {
        ProtocolEntry entry = protocols.get(version);
        if (entry == null) {
            throw new IllegalArgumentException("No protocol registered for version " + version);
        }
        int index = Collections.binarySearch(sortedProtocols, entry);
        return new ProtocolBehaviorSet(sortedProtocols.stream().skip(index).map(ProtocolEntry::behavior).filter(Objects::nonNull).toList());
    }

    public static ProtocolBehaviorSet latestBehaviorSet() {
        return getBehaviorSet(SharedConstants.getCurrentVersion().getProtocolVersion());
    }

    public static void register(IProtocolExt protocol, @Nullable ProtocolBehavior behavior) {
        ProtocolEntry existingProtocol = protocols.get(protocol.getValue());
        if (existingProtocol != null) {
            throw new IllegalArgumentException("Can't register protocol " + protocol.getName() + " with id " + protocol.getValue() + " because a protocol " + existingProtocol.protocol.getName() + " already exists with the same id");
        }

        for (ProtocolEntry existing : sortedProtocols) {
            if (existing.protocol.getName().equals(protocol.getName())) {
                throw new IllegalArgumentException("Can't register protocol " + protocol.getName() + " with id " + protocol.getValue() + " because a protocol with id " + existing.protocol.getValue() + " already exists with the same name");
            }
        }

        ProtocolEntry newEntry = new ProtocolEntry(protocol, behavior);
        int index = Collections.binarySearch(sortedProtocols, newEntry);
        // index will be negative because the protocol id won't be found (checked above)
        index = -index - 1;

        if (index != 0) {
            IProtocol prevProtocol = sortedProtocols.get(index - 1).protocol;
            if (protocol.getDataVersion() < prevProtocol.getDataVersion()) {
                throw new IllegalArgumentException("Protocol " + protocol.getName() + " has data version (" + protocol.getDataVersion() + ") less than the previous protocol's (" + prevProtocol.getName() + ") data version (" + prevProtocol.getDataVersion() + ")");
            }
        }
        if (index != sortedProtocols.size()) {
            IProtocol nextProtocol = sortedProtocols.get(index).protocol;
            if (protocol.getDataVersion() > nextProtocol.getDataVersion()) {
                throw new IllegalArgumentException("Protocol " + protocol.getName() + " has data version (" + protocol.getDataVersion() + ") greater than the next protocol's (" + nextProtocol.getName() + ") data version (" + nextProtocol.getDataVersion() + ")");
            }
        }

        protocols.put(protocol.getValue(), newEntry);
        sortedProtocols.add(index, newEntry);
    }

    public static IProtocolExt getMajorRelease(IProtocolExt protocol) {
        int index = Collections.binarySearch(sortedProtocols, new ProtocolEntry(protocol, null));
        if (index < 0) {
            throw new IllegalArgumentException("Trying to get major release for unregistered protocol");
        }

        for (; index >= 0; index--) {
            protocol = sortedProtocols.get(index).protocol;
            if (protocol.isMajorRelease()) {
                return protocol;
            }
        }
        return protocol;
    }

    public static List<IProtocol> getMinorReleases(IProtocolExt protocol) {
        int index = Collections.binarySearch(sortedProtocols, new ProtocolEntry(protocol, null));
        if (index < 0) {
            throw new IllegalArgumentException("Trying to get minor releases for unregistered protocol");
        }
        if (index != 0 && !protocol.isMajorRelease()) {
            throw new UnsupportedOperationException("Cannot get the minor releases of a minor release");
        }

        List<IProtocol> result = new ArrayList<>();
        result.add(protocol);
        for (index++; index < sortedProtocols.size(); index++) {
            IProtocolExt p = sortedProtocols.get(index).protocol;
            if (p.isMajorRelease()) {
                break;
            }
            result.add(p);
        }
        return result;
    }

    private record ProtocolEntry(IProtocolExt protocol, @Nullable ProtocolBehavior behavior) implements Comparable<ProtocolEntry> {
        @Override
        public int compareTo(ProtocolEntry other) {
            return Integer.compare(protocol.getValue(), other.protocol.getValue());
        }
    }

}
