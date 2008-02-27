package org.seasar.cms.pluggable;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.seasar.framework.container.factory.ResourceResolver;
import org.seasar.framework.util.ResourceUtil;
import org.seasar.framework.util.URLUtil;

public class PluggableResourceResolver implements ResourceResolver {

    private static final char COLON = ':';

    public PluggableResourceResolver() {
    }

    public InputStream getInputStream(final String path) {
        URL url = getURL(path);
        if (url == null) {
            return null;
        }
        return URLUtil.openStream(url);
    }

    protected URL getURL(final String path) {
        if (path.indexOf(COLON) >= 0) {
            try {
                return new URL(path);
            } catch (MalformedURLException ignore) {
            }
        }
        return ResourceUtil.getResourceNoException(path);
    }
}
