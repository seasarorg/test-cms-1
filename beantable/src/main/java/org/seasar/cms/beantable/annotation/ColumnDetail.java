package org.seasar.cms.beantable.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.seasar.cms.beantable.JDBCType;

/**
 * @author YOKOTA Takehiko
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ColumnDetail {
    String value() default "";

    String name() default "";

    JDBCType type() default JDBCType.NONE;

    boolean index() default false;
}
