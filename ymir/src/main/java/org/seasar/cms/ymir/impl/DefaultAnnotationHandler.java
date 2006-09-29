package org.seasar.cms.ymir.impl;

import org.seasar.cms.ymir.AnnotationHandler;
import org.seasar.cms.ymir.AttributeHandler;
import org.seasar.cms.ymir.Authorizer;

public class DefaultAnnotationHandler implements AnnotationHandler {

    public AttributeHandler[] getInjectedScopeAttributes(Object component) {
        return new AttributeHandler[0];
    }

    public AttributeHandler[] getOutjectedScopeAttributes(Object component) {
        return new AttributeHandler[0];
    }

    public Authorizer[] getAuthorizers(Object component) {
        return new Authorizer[0];
    }
}
