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
    String name();

    JDBCType type();

    String defaultValue();

    Constraint[] constraint() default {};

    boolean index();
}
