package org.seasar.cms.framework.response.constructor.impl;

import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.impl.VoidResponse;

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
