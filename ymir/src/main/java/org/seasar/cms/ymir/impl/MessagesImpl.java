package org.seasar.cms.ymir.impl;

import java.util.Locale;

import org.seasar.cms.ymir.Messages;
import org.seasar.kvasir.util.collection.I18NProperties;
import org.seasar.kvasir.util.io.Resource;
import org.seasar.kvasir.util.io.impl.JavaResource;

public class MessagesImpl implements Messages {

    private I18NProperties messages_;

    public MessagesImpl(String path) {

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

        messages_ = new I18NProperties(resource.getParentResource(), baseName,
                suffix);
    }

    public String getProperty(String name, Locale locale) {

        return messages_.getProperty(name, locale);
    }

    public String getProperty(String name) {

        return messages_.getProperty(name);
    }
}
