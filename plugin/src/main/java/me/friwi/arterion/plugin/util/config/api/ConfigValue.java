package me.friwi.arterion.plugin.util.config.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigValue {
    String name() default "";

    boolean required() default true;

    String fallback() default "";
}
