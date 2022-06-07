package net.earthcomputer.multiconnect.ap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Handler {
    /**
     * Required for serverbound handlers, the version before which this packet must be handled
     */
    int protocol() default -1;
}
