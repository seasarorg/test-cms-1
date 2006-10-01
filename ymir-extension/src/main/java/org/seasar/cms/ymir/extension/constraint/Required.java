package org.seasar.cms.ymir.extension.constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.seasar.cms.ymir.extension.annotation.ConstraintAnnotation;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE, ElementType.METHOD })
@ConstraintAnnotation(RequiredConstraintFactory.class)
public @interface Required {

    String[] value() default {};
}
