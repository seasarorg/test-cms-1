package org.seasar.cms.ymir.impl;

import java.lang.reflect.Method;

import org.seasar.cms.ymir.AnnotationHandler;
import org.seasar.cms.ymir.AttributeHandler;
import org.seasar.cms.ymir.Constraint;

public class DefaultAnnotationHandler implements AnnotationHandler {

    public AttributeHandler[] getInjectedScopeAttributes(Object component) {
        return new AttributeHandler[0];
    }

    public AttributeHandler[] getOutjectedScopeAttributes(Object component) {
        return new AttributeHandler[0];
    }

    public Constraint[] getConstraints(Object component, Method action,
            boolean includeCommonConstraints) {
        return new Constraint[0];
    }
}
