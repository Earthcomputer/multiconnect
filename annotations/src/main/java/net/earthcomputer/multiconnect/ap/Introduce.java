package net.earthcomputer.multiconnect.ap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Repeatable(DualIntroduce.class)
public @interface Introduce {
    boolean booleanValue() default false;
    int[] intValue() default {};
    double[] doubleValue() default {};
    String[] stringValue() default {};

    boolean defaultConstruct() default false;
    String compute() default "";

    Direction direction() default Direction.AUTO;

    enum Direction {
        FROM_NEWER, FROM_OLDER, AUTO
    }
}
