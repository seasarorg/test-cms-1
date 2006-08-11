package org.seasar.cms.pluggable;

import org.seasar.framework.container.factory.PathResolver;
import org.seasar.framework.util.ResourceUtil;

public class PluggablePathResolver implements PathResolver {

    private static final char COLON = ':';

    public String resolvePath(String context, String path) {
        if (path.indexOf(COLON) >= 0) {
            return path;
        } else {
            return ResourceUtil.getResourceNoException(path).toExternalForm();
        }
    }
}
