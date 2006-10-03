package org.seasar.cms.ymir.extension.constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.seasar.cms.ymir.extension.ConstraintType;
import org.seasar.cms.ymir.extension.annotation.ConstraintAnnotation;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE, ElementType.METHOD })
@ConstraintAnnotation(type = ConstraintType.VALIDATION, factory = NumericConstraintFactory.class)
public @interface Numeric {

    String[] value() default {};

    String[] property() default {};

    boolean integer() default true;

    double greaterThan() default Double.MIN_VALUE;

    double greaterEqual() default Double.MIN_VALUE;

    double lessThan() default Double.MAX_VALUE;

    double lessEqual() default Double.MAX_VALUE;
}
