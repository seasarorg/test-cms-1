package org.seasar.cms.ymir.response.constructor.impl;

import java.io.InputStream;

import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.response.SelfContainedResponse;
import org.seasar.cms.ymir.response.VoidResponse;
import org.seasar.cms.ymir.response.constructor.ResponseConstructor;

public class InputStreamResponseConstructor implements ResponseConstructor {

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
