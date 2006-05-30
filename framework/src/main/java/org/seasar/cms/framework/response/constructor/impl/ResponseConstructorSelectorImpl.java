package org.seasar.cms.framework.response.constructor.impl;

import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.framework.response.constructor.ResponseConstructor;
import org.seasar.cms.framework.response.constructor.ResponseConstructorSelector;

public class ResponseConstructorSelectorImpl implements
    ResponseConstructorSelector {

    private Map constructorMap_ = new HashMap();

    public ResponseConstructor getResponseConstructor(Class type) {

        ResponseConstructor constructor = (ResponseConstructor) constructorMap_
            .get(type.getName());
        if (constructor != null) {
            return constructor;
        } else {
            throw new RuntimeException(
                "ResponseConstructor does not exist for class: " + type);
        }
    }

    public void add(ResponseConstructor constructor) {

        constructorMap_.put(constructor.getTargetClassName(), constructor);
    }
}
