package org.seasar.cms.framework;

public interface ResponseConstructorSelector {

    ResponseConstructor getResponseConstructor(Class type);
}
