package org.seasar.cms.framework.impl;

import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.impl.StringResponseConstructor.Strategy;

public class PageStrategy implements Strategy {

    public static final String SCHEME = "page";

    public String getScheme() {

        return SCHEME;
    }

    public Response constructResponse(String path) {

        return new ResponseImpl(path, false);
    }
}
