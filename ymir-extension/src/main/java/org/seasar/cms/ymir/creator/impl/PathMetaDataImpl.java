package org.seasar.cms.ymir.creator.impl;

import java.io.File;

import org.seasar.cms.ymir.creator.PathMetaData;

public class PathMetaDataImpl implements PathMetaData {

    private String path_;

    private String method_;

    private boolean denied_;

    private String componentName_;

    private String className_;

    private String actionName_;

    private String defaultPath_;

    private File sourceFile_;

    private File templateFile_;

    public PathMetaDataImpl(String path, String method, boolean denied,
        String componentName, String className, String actionName,
        String defaultPath, File sourceFile, File templateFile) {

        path_ = path;
        method_ = method;
        denied_ = denied;
        componentName_ = componentName;
        className_ = className;
        actionName_ = actionName;
        defaultPath_ = defaultPath;
        sourceFile_ = sourceFile;
        templateFile_ = templateFile;
    }

    public String getMethod() {

        return method_;
    }

    public String getPath() {

        return path_;
    }

    public boolean isDenied() {

        return denied_;
    }

    public String getComponentName() {

        return componentName_;
    }

    public String getClassName() {

        return className_;
    }

    public String getActionName() {
        return actionName_;
    }

    public String getDefaultPath() {
        return defaultPath_;
    }

    public File getSourceFile() {

        return sourceFile_;
    }

    public File getTemplateFile() {

        return templateFile_;
    }
}
