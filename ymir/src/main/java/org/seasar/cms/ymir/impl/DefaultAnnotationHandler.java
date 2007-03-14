package org.seasar.cms.ymir.impl;

import java.lang.reflect.Method;

import org.seasar.cms.ymir.AnnotationHandler;
import org.seasar.cms.ymir.ScopeHandler;
import org.seasar.cms.ymir.Constraint;

public class DefaultAnnotationHandler implements AnnotationHandler {

    public ScopeHandler[] getInjectedScopeAttributes(Object component) {
        return new ScopeHandler[0];
    }

    public ScopeHandler[] getOutjectedScopeAttributes(Object component) {
        return new ScopeHandler[0];
    }

    public Constraint[] getConstraints(Object component, Method action) {
        return new Constraint[0];
    }
}
