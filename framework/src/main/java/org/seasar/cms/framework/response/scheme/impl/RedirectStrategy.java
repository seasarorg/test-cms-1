package org.seasar.cms.framework.response.scheme.impl;

import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.impl.ResponseImpl;
import org.seasar.cms.framework.response.scheme.Strategy;

public class RedirectStrategy implements Strategy {

    private static final String SCHEME = "redirect";

    public String getScheme() {

        return SCHEME;
    }

    public Response constructResponse(String path) {

        return new ResponseImpl(path, true);
    }
}
