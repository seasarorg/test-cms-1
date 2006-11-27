package org.seasar.cms.ymir.extension.creator;

import java.io.File;
import java.io.OutputStream;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.MatchedPathMapping;
import org.seasar.cms.ymir.ResponseCreator;
import org.seasar.cms.ymir.Updater;

public interface SourceCreator extends Updater {

    String PARAM_PREFIX = "__ymir__";

    String PARAM_TASK = PARAM_PREFIX + "task";

    String PATH_PREFIX = "/" + PARAM_PREFIX + "/";

    String getRootPackageName();

    String getPagePackageName();

    String getDtoPackageName();

    String getDaoPackageName();

    String getDxoPackageName();

    File getWebappSourceRoot();

    File getSourceDirectory();

    File getResourcesDirectory();

    MatchedPathMapping findMatchedPathMapping(String path, String method);

    boolean isDenied(String path, String method);

    String getComponentName(String path, String method);

    String getActionName(String path, String method);

    String getClassName(String componentName);

    File getSourceFile(String className);

    Template getTemplate(String path);

    ResponseCreator getResponseCreator();

    boolean writeSourceFile(ClassDesc classDesc, ClassDescSet classDescSet);

    SourceGenerator getSourceGenerator();

    void writeString(String string, File file);

    void writeString(String string, OutputStream os);

    Class getClass(String className);

    Application getApplication();

    ServletContext getServletContext();

    ClassDescBag gatherClassDescs(PathMetaData[] pathMetaDatas);

    void updateClasses(ClassDescBag classDescBag, boolean mergeMethod);

    Properties getSourceCreatorProperties();

    void saveSourceCreatorProperties();

    ClassDesc newClassDesc(String className);

    void mergeWithExistentClass(ClassDesc desc, boolean mergeMethod);

    TemplateProvider getTemplateProvider();

    String getEncoding();

    boolean shouldUpdate(Application application);

    HttpServletRequest getHttpServletRequest();

    HttpServletResponse getHttpServletResponse();
}
