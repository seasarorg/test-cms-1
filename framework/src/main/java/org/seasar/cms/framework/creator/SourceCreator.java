package org.seasar.cms.framework.creator;

public interface SourceCreator {

    String getDtoPackageName();

    String getComponentName(String path, String method);

    String getActionName(String path, String method);

    String getClassName(String componentName);

    ClassDesc[] update(String path, String method);
}
