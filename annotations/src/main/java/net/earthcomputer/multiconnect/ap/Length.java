package net.earthcomputer.multiconnect.ap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Length {
    Types type() default Types.VAR_INT;
    int constant() default -1;
    String compute() default "";
    boolean remainingBytes() default false;
    boolean raw() default false;
    int max() default -1;
}
