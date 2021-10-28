package net.earthcomputer.multiconnect.ap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Message {
    Class<?> variantOf() default Object.class;
    int minVersion() default -1;
    int maxVersion() default -1;
    boolean tailrec() default false;
}
