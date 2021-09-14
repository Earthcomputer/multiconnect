package net.earthcomputer.multiconnect.ap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Message {
    Protocol translateFromNewer() default @Protocol(value = -1, type = Object.class);
    Protocol translateFromOlder() default @Protocol(value = -1, type = Object.class);
}
