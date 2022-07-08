package net.earthcomputer.multiconnect.ap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Repeatable(CustomFixes.class)
public @interface CustomFix {
    String value();
    boolean recursive() default false;
    Introduce.Direction direction() default Introduce.Direction.AUTO;
}
