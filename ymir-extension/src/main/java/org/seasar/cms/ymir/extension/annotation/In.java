package org.seasar.cms.ymir.extension.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface In {

    Class value() default Object.class;

    String name() default "";

    Class scopeClass() default Object.class;

    String scopeName() default "";
}
