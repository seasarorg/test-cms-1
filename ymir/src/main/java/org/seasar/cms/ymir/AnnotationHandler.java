package org.seasar.cms.ymir;

public interface AnnotationHandler {

    AttributeHandler[] getInjectedScopeAttributes(Object component);

    AttributeHandler[] getOutjectedScopeAttributes(Object component);

    Authorizer[] getAuthorizers(Object component);
}
