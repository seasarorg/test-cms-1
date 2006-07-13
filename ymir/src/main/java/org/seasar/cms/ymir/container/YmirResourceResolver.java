package org.seasar.cms.ymir.container;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.seasar.framework.container.factory.ClassPathResourceResolver;
import org.seasar.framework.container.factory.ResourceResolver;
import org.seasar.framework.exception.IORuntimeException;

public class YmirResourceResolver implements ResourceResolver {

    private static final char COLON = ':';

    private static final String SCHEME_DEPENDS = "depends:";

    private static final String SCHEME_JAR = "jar:";

    private static final String SUFFIX_JAR = ".jar!";

    private ResourceResolver urlResourceResolver_ = new URLResourceResolver();

    private ResourceResolver classPathResourceResolver_ = new ClassPathResourceResolver();

    public InputStream getInputStream(String path) {

        // TODO depends:の処理はPathResolverでやるべき。
        if (path.startsWith(SCHEME_DEPENDS)) {
            String jarName = path.substring(SCHEME_DEPENDS.length());
            URL url = getResourceURL(jarName);
            if (url == null) {
                throw new RuntimeException(
                    "Can't find depending components.xml for " + jarName);
            }
            try {
                return url.openStream();
            } catch (IOException ex) {
                throw new IORuntimeException(ex);
            }
        } else if (path.indexOf(COLON) >= 0) {
            return urlResourceResolver_.getInputStream(path);
        } else {
            return classPathResourceResolver_.getInputStream(path);
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
