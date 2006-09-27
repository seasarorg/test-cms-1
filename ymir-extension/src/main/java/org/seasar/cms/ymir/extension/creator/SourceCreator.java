package org.seasar.cms.ymir.extension.creator;

import java.io.File;
import java.util.Properties;

import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.ResponseCreator;
import org.seasar.cms.ymir.Updater;

public interface SourceCreator extends Updater {

    String getRootPackageName();

    String getPagePackageName();

    String getDtoPackageName();

    String getDaoPackageName();

    String getDxoPackageName();

    File getWebappSourceRoot();

    File getSourceDirectory();

    File getResourcesDirectory();

    boolean isDenied(String path, String method);

    String getComponentName(String path, String method);

    String getActionName(String path, String method);

    String getClassName(String componentName);

    File getSourceFile(String className);

    File getTemplateFile(String className);

    ResponseCreator getResponseCreator();

    boolean writeSourceFile(ClassDesc classDesc, ClassDescSet classDescSet);

    SourceGenerator getSourceGenerator();

    void writeString(String string, File file);

    Class getClass(String className);

    Application getApplication();

    ClassDescBag gatherClassDescs(PathMetaData[] pathMetaDatas);

    void updateClasses(ClassDescBag classDescBag, boolean mergeMethod);

    Properties getSourceCreatorProperties();

    void saveSourceCreatorProperties();

    ClassDesc newClassDesc(String className);

    void mergeWithExistentClass(ClassDesc desc, boolean mergeMethod);
}
