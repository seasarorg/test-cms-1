package org.seasar.cms.ymir.response.scheme.impl;

import java.io.InputStream;

import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.impl.SelfContainedResponse;
import org.seasar.cms.ymir.response.scheme.Strategy;

public class ResourceStrategy implements Strategy {

    public static final String SCHEME = "resource";

    public String getScheme() {

        return SCHEME;
    }

    public Response constructResponse(String path, Object component) {

        InputStream in = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(path);
        if (in == null) {
            throw new IllegalArgumentException("Resource does not exist: path="
                + path + ", classLoader="
                + Thread.currentThread().getContextClassLoader());
        }
        return new SelfContainedResponse(in);
    }
}
