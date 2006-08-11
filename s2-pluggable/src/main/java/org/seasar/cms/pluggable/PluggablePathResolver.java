package org.seasar.cms.pluggable;

import org.seasar.framework.container.factory.PathResolver;

public class PluggablePathResolver implements PathResolver {

    public String resolvePath(String context, String path) {
        return Thread.currentThread().getContextClassLoader().getResource(path)
                .toExternalForm();
    }
}
