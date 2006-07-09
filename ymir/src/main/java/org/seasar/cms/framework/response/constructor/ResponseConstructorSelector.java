package org.seasar.cms.framework.response.constructor;

public interface ResponseConstructorSelector {

    ResponseConstructor getResponseConstructor(Class type);
}
