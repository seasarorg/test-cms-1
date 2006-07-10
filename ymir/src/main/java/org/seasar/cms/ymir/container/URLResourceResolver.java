package org.seasar.cms.ymir.container;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.seasar.framework.container.factory.ResourceResolver;
import org.seasar.framework.exception.IORuntimeException;

public class URLResourceResolver implements ResourceResolver {

    public InputStream getInputStream(String path) {

        try {
            return new URL(path).openStream();
        } catch (MalformedURLException ex) {
            throw new IORuntimeException(ex);
        } catch (IOException ex) {
            throw new IORuntimeException(ex);
        }
    }
}
