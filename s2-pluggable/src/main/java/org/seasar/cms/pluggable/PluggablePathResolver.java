package org.seasar.cms.pluggable;

import java.net.URL;

import org.seasar.framework.container.factory.PathResolver;
import org.seasar.framework.util.ResourceUtil;

public class PluggablePathResolver implements PathResolver {

    private static final char COLON = ':';

    public String resolvePath(String context, String path) {
        if (path.indexOf(COLON) >= 0) {
            return path;
        } else {
            URL url = ResourceUtil.getResourceNoException(path);
            if (url != null) {
                return url.toExternalForm();
            } else {
                return path;
            }
        }
    }
}
