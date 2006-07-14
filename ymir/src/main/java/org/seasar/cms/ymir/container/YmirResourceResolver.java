package org.seasar.cms.ymir.container;

import java.io.InputStream;

import org.seasar.framework.container.factory.ClassPathResourceResolver;
import org.seasar.framework.container.factory.ResourceResolver;

// TODO S2.4-beta-4からは不要の予定。
public class YmirResourceResolver implements ResourceResolver {

    private static final char COLON = ':';

    private ResourceResolver urlResourceResolver_ = new URLResourceResolver();

    private ResourceResolver classPathResourceResolver_ = new ClassPathResourceResolver();

    public InputStream getInputStream(String path) {

        if (path.indexOf(COLON) >= 0) {
            return urlResourceResolver_.getInputStream(path);
        } else {
            return classPathResourceResolver_.getInputStream(path);
        }
    }
}
