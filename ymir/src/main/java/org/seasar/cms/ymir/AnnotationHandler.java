package org.seasar.cms.ymir;

import java.lang.reflect.Method;

public interface AnnotationHandler {

    ScopeHandler[] getInjectedScopeAttributes(Object component);

    ScopeHandler[] getOutjectedScopeAttributes(Object component);

    Constraint[] getConstraints(Object component, Method action);
}
