package org.seasar.cms.ymir.extension.creator.impl;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Annotation1 {
    int intValue() default 0;

    byte byteValue() default 0;

    char charValue() default 0;

    long longValue() default 0;

    short shortValue() default 0;

    float floatValue() default 0;

    double doubleValue() default 0;

    boolean booleanValue() default false;

    Enum1 enumValue() default Enum1.VALUE1;

    Class classValue() default Object.class;
}
