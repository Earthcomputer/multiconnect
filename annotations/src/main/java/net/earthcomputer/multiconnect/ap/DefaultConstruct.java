package net.earthcomputer.multiconnect.ap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface DefaultConstruct {
    Class<?> subType() default Object.class;
    boolean booleanValue() default false;
    long[] intValue() default {};
    double[] doubleValue() default {};
    String[] stringValue() default {};
    String compute() default "";
}
