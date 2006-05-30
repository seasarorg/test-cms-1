package org.seasar.cms.framework.response.constructor.impl;

import java.io.InputStream;

import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.impl.SelfContainedResponse;
import org.seasar.cms.framework.impl.VoidResponse;

public class InputStreamResponseConstructor extends AbstractResponseConstructor {

    public String getTargetClassName() {

        return InputStream.class.getName();
    }

    public Response constructResponse(Object value) {

        InputStream inputStream = (InputStream) value;
        if (inputStream == null) {
            return VoidResponse.INSTANCE;
        } else {
            return new SelfContainedResponse(inputStream);
        }
    }
}
