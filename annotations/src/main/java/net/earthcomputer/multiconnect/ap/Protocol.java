package net.earthcomputer.multiconnect.ap;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({})
public @interface Protocol {
    int value();
    Class<?> type();
}
