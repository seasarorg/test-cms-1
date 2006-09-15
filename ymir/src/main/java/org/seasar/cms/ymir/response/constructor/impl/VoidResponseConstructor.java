package org.seasar.cms.ymir.response.constructor.impl;

import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.impl.PassthroughResponse;
import org.seasar.cms.ymir.response.constructor.ResponseConstructor;

public class VoidResponseConstructor implements ResponseConstructor {

    public Class getTargetClass() {

        return Void.TYPE;
    }

    public Response constructResponse(Object component, Object returnValue) {

        return PassthroughResponse.INSTANCE;
    }
}
