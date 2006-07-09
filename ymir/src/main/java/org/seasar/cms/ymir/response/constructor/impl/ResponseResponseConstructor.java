package org.seasar.cms.ymir.response.constructor.impl;

import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.impl.VoidResponse;

public class ResponseResponseConstructor extends AbstractResponseConstructor {

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
