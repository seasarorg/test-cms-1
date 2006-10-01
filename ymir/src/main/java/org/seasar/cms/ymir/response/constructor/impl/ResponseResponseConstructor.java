package org.seasar.cms.ymir.response.constructor.impl;

import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.response.VoidResponse;
import org.seasar.cms.ymir.response.constructor.ResponseConstructor;

public class ResponseResponseConstructor implements ResponseConstructor {

    public Class getTargetClass() {

        return Response.class;
    }

    public Response constructResponse(Object component, Object returnValue) {

        Response response = (Response) returnValue;
        if (response == null) {
            response = VoidResponse.INSTANCE;
        }

        return response;
    }
}
