package org.seasar.cms.classbuilder.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.seasar.framework.container.annotation.tiger.AutoBindingType;
import org.seasar.framework.container.annotation.tiger.InstanceType;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD })
public @interface Component
{
    InstanceType instance() default InstanceType.SINGLETON;


    AutoBindingType autoBinding() default AutoBindingType.AUTO;


    boolean externalBinding() default false;
}