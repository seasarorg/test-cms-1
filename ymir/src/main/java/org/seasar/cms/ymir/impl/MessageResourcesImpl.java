package org.seasar.cms.ymir.impl;

import java.util.Locale;

import org.seasar.cms.ymir.MessageResources;
import org.seasar.kvasir.util.collection.I18NProperties;
import org.seasar.kvasir.util.io.Resource;
import org.seasar.kvasir.util.io.impl.JavaResource;

public class MessageResourcesImpl implements MessageResources {

    private I18NProperties resources_;

    public MessageResourcesImpl(String path) {

        Resource resource = new JavaResource(path, Thread.currentThread()
                .getContextClassLoader());

        String name;
        int slash = path.lastIndexOf('/');
        if (slash >= 0) {
            name = path.substring(slash + 1);
        } else {
            name = path;
        }
        String baseName;
        String suffix;
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            baseName = name.substring(0, dot);
            suffix = name.substring(dot);
        } else {
            baseName = name;
            suffix = "";
        }

        resources_ = new I18NProperties(resource.getParentResource(), baseName,
                suffix);
    }

    public String getProperty(String name, Locale locale) {

        return resources_.getProperty(name, locale);
    }

    public String getProperty(String name) {

        return resources_.getProperty(name);
    }
}
