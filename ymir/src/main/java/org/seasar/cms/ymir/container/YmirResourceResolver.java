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

    private ResourceResolver urlResourceResolver_ = new URLResourceResolver();

    private ResourceResolver classPathResourceResolver_ = new ClassPathResourceResolver();

    public InputStream getInputStream(String path) {

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

        URL[] urls = ContainerUtils.getResourceURLs(Globals.COMPONENTS_DICON);
        for (int i = 0; i < urls.length; i++) {
            String externalForm = urls[i].toExternalForm();
            int idx = externalForm.indexOf(path);
            if (idx >= 0 && externalForm.charAt(idx - 1) == '/'
                && externalForm.charAt(idx + path.length()) == '!') {
                return urls[i];
            }
        }
        return null;
    }
}
