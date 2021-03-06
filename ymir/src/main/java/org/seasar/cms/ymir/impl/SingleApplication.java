package org.seasar.cms.ymir.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Enumeration;

import javax.servlet.ServletContext;

import org.seasar.cms.pluggable.Configuration;
import org.seasar.cms.pluggable.hotdeploy.LocalHotdeployS2Container;
import org.seasar.cms.ymir.PathMappingProvider;
import org.seasar.framework.container.S2Container;

public class SingleApplication extends AbstractApplication {

    public static final String ID_DEFAULT = "";

    private ServletContext context_;

    private Configuration config_;

    private Class referenceClass_;

    private S2Container container_;

    public SingleApplication(ServletContext context, Configuration config,
            Class referenceClass, S2Container container,
            LocalHotdeployS2Container ondemandContainer,
            PathMappingProvider pathMappingProvider) {
        super(ID_DEFAULT, ondemandContainer, pathMappingProvider);
        context_ = context;
        config_ = config;
        referenceClass_ = referenceClass;
        container_ = container;
    }

    public S2Container getS2Container() {
        return container_;
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

    public Enumeration propertyNames() {
        return config_.propertyNames();
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

    public boolean isUnderDevelopment() {
        return true;
    }

    public boolean isResourceExists(String path) {
        try {
            return (context_.getResource(path) != null);
        } catch (MalformedURLException ex) {
            return false;
        }
    }

    public String getWebappRoot() {
        return context_.getRealPath("");
    }
}
