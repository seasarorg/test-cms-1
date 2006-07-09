package org.seasar.cms.framework.creator;

import java.io.File;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;

public interface SourceCreator {

    String getPagePackageName();

    String getDtoPackageName();

    String getDaoPackageName();

    String getDxoPackageName();

    File getWebappDirectory();

    boolean isDenied(String path, String method);

    String getComponentName(String path, String method);

    String getActionName(String path, String method);

    String getDefaultPath(String path, String method);

    String getClassName(String componentName);

    File getSourceFile(String className);

    File getTemplateFile(String className);

    Response update(String path, String method, Request request);
}