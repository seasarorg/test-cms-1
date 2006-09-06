package org.seasar.cms.ymir.response.constructor.impl;

import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.impl.VoidResponse;
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

        ResponseConstructor responseConstructor;
        if (responseConstructorSelector_.hasResponseConstructor(returnValue
                .getClass())) {
            responseConstructor = responseConstructorSelector_
                    .getResponseConstructor(returnValue.getClass());
        } else if (responseConstructorSelector_
                .hasResponseConstructor(String.class)) {
            responseConstructor = responseConstructorSelector_
                    .getResponseConstructor(String.class);
            returnValue = returnValue.toString();
        } else {
            throw new RuntimeException(
                    "Can't construct response for return value: class="
                            + returnValue.getClass() + ", value=" + returnValue);
        }

        return responseConstructor.constructResponse(component, returnValue);
    }

    public void setResponseConstructorSelector(
            ResponseConstructorSelector responseConstructorSelector) {
        responseConstructorSelector_ = responseConstructorSelector;
    }
}
