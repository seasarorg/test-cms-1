package org.seasar.cms.framework.response.constructor.impl;

import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.impl.StatusResponse;

public class IntResponseConstructor extends AbstractResponseConstructor {

    public Class getTargetClass() {

        return Integer.TYPE;
    }

    public Response constructResponse(Object component, Object returnValue) {

        return new StatusResponse(((Integer) returnValue).intValue());
    }
}
