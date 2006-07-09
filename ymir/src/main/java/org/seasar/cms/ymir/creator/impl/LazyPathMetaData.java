package org.seasar.cms.ymir.creator.impl;

import java.io.File;

import org.seasar.cms.ymir.creator.PathMetaData;
import org.seasar.cms.ymir.creator.SourceCreator;

public class LazyPathMetaData implements PathMetaData {

    private SourceCreator sourceCreator_;

    private String path_;

    private String method_;

    private boolean deniedLoaded_;

    private boolean denied_;

    private String componentName_;

    private boolean componentNameLoaded_;

    private String className_;

    private boolean classNameLoaded_;

    private String actionName_;

    private boolean actionNameLoaded_;

    private String defaultPath_;

    private boolean defaultPathLoaded_;

    private File sourceFile_;

    private boolean sourceFileLoaded_;

    private File templateFile_;

    private boolean templateFileLoaded_;

    public LazyPathMetaData(SourceCreator sourceCreator, String path,
        String method) {

        sourceCreator_ = sourceCreator;
        path_ = path;
        method_ = method;
    }

    public String getMethod() {

        return method_;
    }

    public String getPath() {

        return path_;
    }

    public boolean isDenied() {

        if (!deniedLoaded_) {
            denied_ = sourceCreator_.isDenied(path_, method_);
        }
        return denied_;
    }

    public String getComponentName() {

        if (!componentNameLoaded_) {
            componentName_ = sourceCreator_.getComponentName(path_, method_);
        }
        return componentName_;
    }

    public String getClassName() {

        if (!classNameLoaded_) {
            className_ = sourceCreator_.getClassName(getComponentName());
        }
        return className_;
    }

    public String getActionName() {

        if (!actionNameLoaded_) {
            actionName_ = sourceCreator_.getActionName(path_, method_);
        }
        return actionName_;
    }

    public String getDefaultPath() {

        if (!defaultPathLoaded_) {
            defaultPath_ = sourceCreator_.getDefaultPath(path_, method_);
        }
        return defaultPath_;
    }

    public File getSourceFile() {

        if (!sourceFileLoaded_) {
            sourceFile_ = sourceCreator_.getSourceFile(getClassName() + "Base");
        }
        return sourceFile_;
    }

    public File getTemplateFile() {

        if (!templateFileLoaded_) {
            templateFile_ = sourceCreator_.getTemplateFile(path_);
        }
        return templateFile_;
    }
}
