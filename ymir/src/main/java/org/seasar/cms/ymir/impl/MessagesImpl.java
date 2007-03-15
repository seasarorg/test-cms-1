package org.seasar.cms.ymir.impl;

import java.util.Locale;

import org.seasar.cms.ymir.Messages;
import org.seasar.cms.ymir.Ymir;
import org.seasar.cms.ymir.YmirContext;
import org.seasar.kvasir.util.collection.I18NProperties;
import org.seasar.kvasir.util.io.Resource;
import org.seasar.kvasir.util.io.impl.JavaResource;

public class MessagesImpl implements Messages {

    private String path_;

    private I18NProperties messages_;

    public MessagesImpl(String path) {

        path_ = path;
        init();
    }

    void init() {

        Resource resource = new JavaResource(path_, Thread.currentThread()
                .getContextClassLoader());

        String name;
        int slash = path_.lastIndexOf('/');
        if (slash >= 0) {
            name = path_.substring(slash + 1);
        } else {
            name = path_;
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

        updateMessages();
        return messages_.getProperty(name, locale);
    }

    void updateMessages() {

        if (getYmir().isUnderDevelopment()) {
            init();
        }
    }

    protected Ymir getYmir() {

        return YmirContext.getYmir();
    }

    public String getProperty(String name) {

        updateMessages();
        return messages_.getProperty(name);
    }
}
