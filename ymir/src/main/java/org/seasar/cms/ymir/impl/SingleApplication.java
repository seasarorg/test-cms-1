package org.seasar.cms.ymir.impl;

import java.io.IOException;
import java.io.OutputStream;

import org.seasar.cms.pluggable.Configuration;
import org.seasar.cms.pluggable.hotdeploy.LocalOndemandS2Container;
import org.seasar.cms.ymir.PathMappingProvider;
import org.seasar.framework.container.S2Container;

public class SingleApplication extends AbstractApplication {

    private Configuration config_;

    private String webappRoot_;

    private Class referenceClass_;

    private S2Container container_;

    public SingleApplication(Configuration config, String webappRoot,
            Class referenceClass, S2Container container,
            LocalOndemandS2Container ondemandContainer,
            PathMappingProvider pathMappingProvider) {
        super(ondemandContainer, pathMappingProvider);
        config_ = config;
        webappRoot_ = webappRoot;
        referenceClass_ = referenceClass;
        container_ = container;
    }

    public S2Container getS2Container() {
        return container_;
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

    public boolean isBeingDeveloped() {
        return true;
    }
}
