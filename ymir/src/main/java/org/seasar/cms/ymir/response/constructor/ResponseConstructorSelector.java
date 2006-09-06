package org.seasar.cms.ymir.response.constructor;

public interface ResponseConstructorSelector {

    boolean hasResponseConstructor(Class type);

    ResponseConstructor getResponseConstructor(Class type);
}
