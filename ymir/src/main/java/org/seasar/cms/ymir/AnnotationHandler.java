package org.seasar.cms.ymir;

import java.lang.reflect.Method;

public interface AnnotationHandler {

    ScopeAttribute[] getInjectedScopeAttributes(Object component);

    ScopeAttribute[] getOutjectedScopeAttributes(Object component);

    Constraint[] getConstraints(Object component, Method action);
}
