package org.seasar.cms.ymir.response.constructor;

public interface ResponseConstructorSelector {

    ResponseConstructor getResponseConstructor(Class type);
}
