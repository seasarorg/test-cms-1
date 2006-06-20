package org.seasar.cms.framework.response.constructor;

import org.seasar.cms.framework.Response;

public interface ResponseConstructor {

    String getTargetClassName();

    Response constructResponse(Object component, Object returnValue);
}
