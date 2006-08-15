package org.seasar.cms.ymir.impl;

import java.io.IOException;
import java.io.OutputStream;

import org.seasar.cms.pluggable.Configuration;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;

public class SingleApplication extends AbstractApplication {

    private Configuration config_;

    private String webappRoot_;

    private Class referenceClass_;

    public SingleApplication(Configuration config, String webappRoot,
            Class referenceClass) {
        config_ = config;
        webappRoot_ = webappRoot;
        referenceClass_ = referenceClass;
    }

    public S2Container getS2Container() {
        return SingletonS2ContainerFactory.getContainer();
    }

    public String getWebappRoot() {
        return webappRoot_;
    }

    public Class getReferenceClass() {
        return referenceClass_;
    }

    public String getProperty(String key) {
        return config_.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return config_.getProperty(key, defaultValue);
    }

    public void removeProperty(String key) {
        config_.removeProperty(key);
    }

    public void save(OutputStream out, String header) throws IOException {
        config_.save(out, header);
    }

    public void setProperty(String key, String value) {
        config_.setProperty(key, value);
    }

    public boolean isCapable(Class clazz) {
        return true;
    }
}
