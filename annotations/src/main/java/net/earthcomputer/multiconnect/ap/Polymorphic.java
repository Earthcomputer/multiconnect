package net.earthcomputer.multiconnect.ap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Polymorphic {
    boolean booleanValue() default false;
    long[] intValue() default {};
    double[] doubleValue() default {};
    String[] stringValue() default {};
    boolean otherwise() default false;

    String condition() default "";
}
