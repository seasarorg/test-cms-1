package org.seasar.cms.ymir.impl;

import java.lang.reflect.Method;

import org.seasar.cms.ymir.AnnotationHandler;
import org.seasar.cms.ymir.ScopeAttribute;
import org.seasar.cms.ymir.Constraint;

public class DefaultAnnotationHandler implements AnnotationHandler {

    public ScopeAttribute[] getInjectedScopeAttributes(Object component) {
        return new ScopeAttribute[0];
    }

    public ScopeAttribute[] getOutjectedScopeAttributes(Object component) {
        return new ScopeAttribute[0];
    }

    public Constraint[] getConstraints(Object component, Method action) {
        return new Constraint[0];
    }
}
