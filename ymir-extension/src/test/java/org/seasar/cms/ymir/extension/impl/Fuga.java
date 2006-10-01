package org.seasar.cms.ymir.extension.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.seasar.cms.ymir.extension.annotation.ConstraintAnnotation;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE, ElementType.METHOD })
@ConstraintAnnotation(FugaConstraintFactory.class)
public @interface Fuga {

    String value();
}
