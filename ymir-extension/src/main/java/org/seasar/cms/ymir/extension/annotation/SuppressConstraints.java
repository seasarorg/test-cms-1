package org.seasar.cms.ymir.extension.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.seasar.cms.ymir.extension.ConstraintType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SuppressConstraints {
    ConstraintType[] value() default { ConstraintType.VALIDATION,
        ConstraintType.PERMISSION };
}
