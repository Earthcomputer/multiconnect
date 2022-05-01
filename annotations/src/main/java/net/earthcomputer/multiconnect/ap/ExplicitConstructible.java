package net.earthcomputer.multiconnect.ap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Signifies that this type can be default constructed explicitly via {@code PacketSystem.defaultConstruct}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ExplicitConstructible {
}
