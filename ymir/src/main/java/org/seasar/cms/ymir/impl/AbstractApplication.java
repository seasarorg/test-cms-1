package org.seasar.cms.ymir.impl;

import org.seasar.cms.pluggable.hotdeploy.LocalOndemandS2Container;
import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.PathMappingProvider;

abstract public class AbstractApplication implements Application {

    public static final String KEY_PROJECTROOT = "projectRoot";

    public static final String KEY_WEBAPPSOURCEROOT = "webappSourceRoot";

    public static final String KEY_ROOTPACKAGENAME = "rootPackageName";

    public static final String KEY_RESOURCESDIRECTORY = "resourcesDirectory";

    public static final String KEY_SOURCEDIRECTORY = "sourceDirectory";

    private static final String PROPERTIESFILEPATH = "app.properties";

    private String id_;

    private LocalOndemandS2Container ondemandContainer_;

    private PathMappingProvider pathMappingProvider_;

    protected AbstractApplication(String id,
            LocalOndemandS2Container ondemandContainer,
            PathMappingProvider pathMappingProvider) {
        id_ = id;
        ondemandContainer_ = ondemandContainer;
        pathMappingProvider_ = pathMappingProvider;
    }

    public String getId() {
        return id_;
    }

    public String getProjectRoot() {
        return getProperty(KEY_PROJECTROOT);
    }

    public void setProjectRoot(String projectRoot) {
        setProperty(KEY_PROJECTROOT, projectRoot);
    }

    public String getRootPackageName() {
        return getProperty(KEY_ROOTPACKAGENAME);
    }

    public void setRootPackageName(String rootPackageName) {
        setProperty(KEY_ROOTPACKAGENAME, rootPackageName);
    }

    public String getResourcesDirectory() {
        String resourcesDirectory = getProperty(KEY_RESOURCESDIRECTORY);
        if (resourcesDirectory == null) {
            String projectRoot = getProjectRoot();
            if (projectRoot != null) {
                resourcesDirectory = projectRoot + "/src/main/resources";
            }
        } else {
            String projectRoot = getProjectRoot();
            if (projectRoot != null) {
                resourcesDirectory = projectRoot + "/" + resourcesDirectory;
            }
        }
        return resourcesDirectory;
    }

    public void setResourcesDirectory(String resourcesDirectory) {
        setProperty(KEY_RESOURCESDIRECTORY, resourcesDirectory);
    }

    public String getSourceDirectory() {
        String sourceDirectory = getProperty(KEY_SOURCEDIRECTORY);
        if (sourceDirectory == null) {
            String projectRoot = getProjectRoot();
            if (projectRoot != null) {
                sourceDirectory = projectRoot + "/src/main/java";
            }
        } else {
            String projectRoot = getProjectRoot();
            if (projectRoot != null) {
                sourceDirectory = projectRoot + "/" + sourceDirectory;
            }
        }
        return sourceDirectory;
    }

    public void setSourceDirectory(String sourceDirectory) {
        setProperty(KEY_SOURCEDIRECTORY, sourceDirectory);
    }

    public String getWebappSourceRoot() {
        String webappSourceRoot = getProperty(KEY_WEBAPPSOURCEROOT);
        if (webappSourceRoot == null) {
            String projectRoot = getProjectRoot();
            if (projectRoot != null) {
                webappSourceRoot = projectRoot + "/src/main/webapp";
            }
        } else {
            String projectRoot = getProjectRoot();
            if (projectRoot != null) {
                webappSourceRoot = projectRoot + "/" + webappSourceRoot;
            }
        }
        return webappSourceRoot;
    }

    public void setWebappSourceRoot(String webappSourceRoot) {
        setProperty(KEY_WEBAPPSOURCEROOT, webappSourceRoot);
    }

    public String getDefaultPropertiesFilePath() {
        String resourcesDirectory = getResourcesDirectory();
        if (resourcesDirectory == null) {
            return null;
        } else {
            return resourcesDirectory + "/" + PROPERTIESFILEPATH;
        }
    }

    public LocalOndemandS2Container getOndemandS2Container() {
        return ondemandContainer_;
    }

    public PathMappingProvider getPathMappingProvider() {
        return pathMappingProvider_;
    }

    public boolean isCapable(Class clazz) {
        return clazz.getName().startsWith(getRootPackageName() + ".");
    }
}
