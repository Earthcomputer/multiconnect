package net.earthcomputer.multiconnect.ap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface FilledArgument {
    FromRegistry fromRegistry() default @FromRegistry(registry = Registries.BLOCK, value = "");
    Registries registry() default Registries.BLOCK;

    @interface FromRegistry {
        Registries registry();
        String value();
    }
}
