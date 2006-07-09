package org.seasar.cms.framework.response.constructor;

import org.seasar.cms.framework.Response;

public interface ResponseConstructor {

    Class getTargetClass();

    Response constructResponse(Object component, Object returnValue);
}
