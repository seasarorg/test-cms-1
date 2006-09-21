package org.seasar.cms.ymir;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import org.seasar.cms.pluggable.hotdeploy.LocalOndemandS2Container;
import org.seasar.framework.container.S2Container;

public interface Application {

    String getWebappRoot();

    String getWebappSourceRoot();

    Class getReferenceClass();

    String getProjectRoot();

    void setProjectRoot(String projectRoot);

    String getRootPackageName();

    void setRootPackageName(String rootPackageName);

    String getSourceDirectory();

    void setSourceDirectory(String sourceDirectory);

    String getResourcesDirectory();

    void setResourcesDirectory(String resourcesDirectory);

    boolean isResourceExists(String path);

    S2Container getS2Container();

    boolean isCapable(Class clazz);

    String getProperty(String key);

    String getProperty(String key, String defaultValue);

    Enumeration propertyNames();

    void setProperty(String key, String value);

    void removeProperty(String key);

    void save(OutputStream out, String header) throws IOException;

    String getDefaultPropertiesFilePath();

    LocalOndemandS2Container getOndemandS2Container();

    PathMappingProvider getPathMappingProvider();

    boolean isBeingDeveloped();
}
