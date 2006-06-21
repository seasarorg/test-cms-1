package org.seasar.cms.framework.response.constructor.impl;

import java.io.InputStream;

import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.impl.SelfContainedResponse;
import org.seasar.cms.framework.impl.VoidResponse;

public class InputStreamResponseConstructor extends AbstractResponseConstructor {

    public Class getTargetClass() {

        return InputStream.class;
    }

    public Response constructResponse(Object component, Object returnValue) {

        InputStream inputStream = (InputStream) returnValue;
        if (inputStream == null) {
            return VoidResponse.INSTANCE;
        } else {
            return new SelfContainedResponse(inputStream);
        }
    }
}
