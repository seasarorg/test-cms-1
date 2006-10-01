package org.seasar.cms.ymir;

import java.lang.reflect.Method;

public interface AnnotationHandler {

    AttributeHandler[] getInjectedScopeAttributes(Object component);

    AttributeHandler[] getOutjectedScopeAttributes(Object component);

    Constraint[] getConstraints(Object component, Method action,
            boolean includeCommonConstraints);
}
