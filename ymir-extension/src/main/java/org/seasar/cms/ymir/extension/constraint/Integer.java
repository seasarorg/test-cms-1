package org.seasar.cms.ymir.extension.constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.seasar.cms.ymir.extension.annotation.ConstraintAnnotation;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE, ElementType.METHOD })
@ConstraintAnnotation(NumericConstraintFactory.class)
public @interface Integer {

    String[] value() default {};

    String[] property() default {};

    int greaterThan() default java.lang.Integer.MIN_VALUE;

    int greaterEqual() default java.lang.Integer.MIN_VALUE;

    int lessThan() default java.lang.Integer.MAX_VALUE;

    int lessEqual() default java.lang.Integer.MAX_VALUE;
}
