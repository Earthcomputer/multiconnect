package net.earthcomputer.multiconnect.ap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface FilledArgument {
    int fromVersion() default -1;
    int toVersion() default -1;
    FromRegistry fromRegistry() default @FromRegistry(registry = Registries.BLOCK, value = "");

    @interface FromRegistry {
        Registries registry();
        String value();
    }
}
