package org.seasar.cms.ymir.impl;

import org.seasar.cms.ymir.AnnotationHandler;
import org.seasar.cms.ymir.AttributeHandler;

public class DefaultAnnotationHandler implements AnnotationHandler {

    public AttributeHandler[] getInjectedScopeAttributes(Object component) {
        return new AttributeHandler[0];
    }

    public AttributeHandler[] getOutjectedScopeAttributes(Object component) {
        return new AttributeHandler[0];
    }
}
