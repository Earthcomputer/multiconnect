package net.earthcomputer.multiconnect.protocols.v1_14_4;

import net.minecraft.world.WorldView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

// because you can't put non-public methods in an interface mixin
public class WorldViewStatics {
    public static final Logger MULTICONNECT_LOGGER = LogManager.getLogger("multiconnect");
    public static final Set<WorldView> multiconnect_hasWarned = Collections.newSetFromMap(new WeakHashMap<>());
}
