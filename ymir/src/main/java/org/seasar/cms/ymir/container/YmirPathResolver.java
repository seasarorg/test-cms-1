package org.seasar.cms.ymir.container;

import java.net.URL;

import org.seasar.framework.container.factory.PathResolver;

public class YmirPathResolver implements PathResolver {

    private static final String SCHEME_DEPENDS = "depends:";

    private static final String SCHEME_JAR = "jar:";

    private static final String SUFFIX_JAR = ".jar!";

    public String resolvePath(String context, String path) {

        if (path.startsWith(SCHEME_DEPENDS)) {
            String jarName = path.substring(SCHEME_DEPENDS.length());
            URL url = getResourceURL(jarName);
            if (url == null) {
                throw new RuntimeException(
                    "Can't find depending components.xml for " + jarName);
            }
            return url.toExternalForm();
        } else {
            return path;
        }
    }

    URL getResourceURL(String path) {

        return getResourceURL(path, ContainerUtils
            .getResourceURLs(Globals.COMPONENTS_DICON));
    }

    URL getResourceURL(String path, URL[] urls) {
        for (int i = 0; i < urls.length; i++) {
            String externalForm = urls[i].toExternalForm();
            if (!externalForm.startsWith(SCHEME_JAR)) {
                continue;
            }
            int end = externalForm.indexOf(SUFFIX_JAR);
            if (end < 0) {
                continue;
            }
            int start = externalForm.lastIndexOf('/', end);
            if (start < 0) {
                continue;
            }
            if (externalForm.substring(start + 1, end).startsWith(path)) {
                return urls[i];
            }
        }
        return null;
    }
}
