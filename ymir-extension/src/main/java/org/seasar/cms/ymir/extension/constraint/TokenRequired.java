package org.seasar.cms.ymir.extension.constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.seasar.cms.ymir.extension.ConstraintType;
import org.seasar.cms.ymir.extension.annotation.ConstraintAnnotation;
import org.seasar.cms.ymir.util.TokenUtils;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE, ElementType.METHOD })
@ConstraintAnnotation(type = ConstraintType.VALIDATION, factory = TokenRequiredConstraintFactory.class)
public @interface TokenRequired {

    String value() default TokenUtils.KEY_TOKEN;
}
