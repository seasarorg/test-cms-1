package org.seasar.cms.ymir.extension.creator.impl;

import java.io.File;

import org.seasar.cms.ymir.extension.creator.PathMetaData;
import org.seasar.cms.ymir.extension.creator.Template;

public class PathMetaDataImpl implements PathMetaData {

    private String path_;

    private String method_;

    private boolean denied_;

    private String componentName_;

    private String className_;

    private String actionName_;

    private String defaultPath_;

    private File sourceFile_;

    private File baseSourceFile_;

    private Template template_;

    public PathMetaDataImpl(String path, String method, boolean denied,
            String componentName, String className, String actionName,
            String defaultPath, File sourceFile, Template template) {

        path_ = path;
        method_ = method;
        denied_ = denied;
        componentName_ = componentName;
        className_ = className;
        actionName_ = actionName;
        defaultPath_ = defaultPath;
        sourceFile_ = sourceFile;
        baseSourceFile_ = toBaseSourceFile(sourceFile);
        template_ = template;
    }

    File toBaseSourceFile(File sourceFile) {

        if (sourceFile == null) {
            return null;
        }

        String name = sourceFile.getName();
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return new File(sourceFile.getParentFile(), name + "Base");
        } else {
            return new File(sourceFile.getParentFile(), name.substring(0, dot)
                    + "Base" + name.substring(dot));
        }
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

    public File getBaseSourceFile() {

        return baseSourceFile_;
    }

    public Template getTemplate() {

        return template_;
    }
}
