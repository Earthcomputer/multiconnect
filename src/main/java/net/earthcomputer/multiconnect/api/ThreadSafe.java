package net.earthcomputer.multiconnect.api;

import java.lang.annotation.*;

/**
 * Indicates that the annotated member may be called on multiple threads at once, or concurrently from the game thread,
 * and therefore the implementation must take care when accessing shared state. For instance methods, this implies that
 * the method is called on the same instance from multiple threads.
 *
 * <p>
 * When a class is annotated, all methods within that class are considered to be {@code @Concurrent}.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ThreadSafe {
    /**
     * Whether the annotated member may also be called from the game thread itself.
     */
    boolean withGameThread() default true;
}
