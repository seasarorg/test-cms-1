package org.seasar.cms.framework.impl;

import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.framework.ResponseConstructor;
import org.seasar.cms.framework.ResponseConstructorSelector;

public class ResponseConstructorSelectorImpl implements
    ResponseConstructorSelector {

    private Map constructorMap_ = new HashMap();

    public ResponseConstructor getResponseConstructor(Class type) {

        return (ResponseConstructor) constructorMap_.get(type.getName());
    }

    public void add(ResponseConstructor constructor) {

        constructorMap_.put(constructor.getTargetClassName(), constructor);
    }
}
