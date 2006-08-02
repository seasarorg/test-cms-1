package org.seasar.cms.ymir.extension.creator;

import java.io.File;
import java.util.Properties;

import org.seasar.cms.pluggable.Configuration;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.ResponseCreator;
import org.seasar.cms.ymir.Updater;

public interface SourceCreator extends Updater {

    String getPagePackageName();

    String getDtoPackageName();

    String getDaoPackageName();

    String getDxoPackageName();

    File getWebappDirectory();

    File getSourceDirectory();

    File getClassesDirectory();

    File getResourcesDirectory();

    boolean isDenied(String path, String method);

    String getComponentName(String path, String method);

    String getActionName(String path, String method);

    String getDefaultPath(String path, String method);

    String getClassName(String componentName);

    File getSourceFile(String className);

    File getTemplateFile(String className);

    Response update(String path, String method, Request request);

    ResponseCreator getResponseCreator();

    boolean writeSourceFile(ClassDesc classDesc, ClassDescSet classDescSet);

    SourceGenerator getSourceGenerator();

    void writeString(String string, File file);

    Class getClass(String className);

    Configuration getConfiguration();

    ClassDescBag gatherClassDescs(PathMetaData[] pathMetaDatas);

    void updateClasses(ClassDescBag classDescBag, boolean mergeMethod);

    Properties getSourceCreatorProperties();

    void saveSourceCreatorProperties();
}
