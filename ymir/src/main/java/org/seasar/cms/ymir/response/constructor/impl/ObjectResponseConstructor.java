package org.seasar.cms.ymir.response.constructor.impl;

import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.response.VoidResponse;
import org.seasar.cms.ymir.response.constructor.ResponseConstructor;
import org.seasar.cms.ymir.response.constructor.ResponseConstructorSelector;

public class ObjectResponseConstructor implements ResponseConstructor {

    private ResponseConstructorSelector responseConstructorSelector_;

    public Class getTargetClass() {

        return Object.class;
    }

    public Response constructResponse(Object component, Object returnValue) {

        if (returnValue == null) {
            return VoidResponse.INSTANCE;
        }

        ResponseConstructor responseConstructor = findResponseConstructor(returnValue
                .getClass());
        if (responseConstructor == null) {
            if (responseConstructorSelector_
                    .hasResponseConstructor(String.class)) {
                responseConstructor = responseConstructorSelector_
                        .getResponseConstructor(String.class);
                returnValue = returnValue.toString();
            } else {
                throw new RuntimeException(
                        "Can't construct response for return value: class="
                                + returnValue.getClass() + ", value="
                                + returnValue);
            }
        }

        return responseConstructor.constructResponse(component, returnValue);
    }

    ResponseConstructor findResponseConstructor(Class clazz) {
        if (clazz == Object.class) {
            // 無限ループを避けるため。
            return null;
        }

        Class type = clazz;
        do {
            if (responseConstructorSelector_.hasResponseConstructor(type)) {
                return responseConstructorSelector_
                        .getResponseConstructor(type);
            }
        } while ((type = type.getSuperclass()) != Object.class);
        Class[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (responseConstructorSelector_
                    .hasResponseConstructor(interfaces[i])) {
                return responseConstructorSelector_
                        .getResponseConstructor(interfaces[i]);
            }
        }
        return null;
    }

    public void setResponseConstructorSelector(
            ResponseConstructorSelector responseConstructorSelector) {
        responseConstructorSelector_ = responseConstructorSelector;
    }
}
