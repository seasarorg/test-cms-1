package org.seasar.cms.framework.impl;

import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.impl.StringResponseConstructor.Strategy;

public class RedirectStrategy implements Strategy {

    private static final String SCHEME = "redirect";

    public String getScheme() {

        return SCHEME;
    }

    public Response constructResponse(String path) {

        return new ResponseImpl(path, true);
    }
}
