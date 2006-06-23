package org.seasar.cms.framework.response.scheme.impl;

import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.impl.StatusResponse;
import org.seasar.cms.framework.response.scheme.Strategy;

public class StatusStrategy implements Strategy {

    private static final String SCHEME = "status";

    public String getScheme() {

        return SCHEME;
    }

    public Response constructResponse(String path, Object component) {

        return new StatusResponse(Integer.parseInt(path));
    }
}
