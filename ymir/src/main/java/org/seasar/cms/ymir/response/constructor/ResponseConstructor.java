package org.seasar.cms.ymir.response.constructor;

import org.seasar.cms.ymir.Response;

public interface ResponseConstructor {

    Class getTargetClass();

    Response constructResponse(Object component, Object returnValue);
}
