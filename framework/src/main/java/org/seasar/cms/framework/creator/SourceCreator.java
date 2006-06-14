package org.seasar.cms.framework.creator;

public interface SourceCreator {

    String getDtoPackageName();

    String getComponentName(String path);

    String getClassName(String componentName);

    ClassDesc[] update(String path);
}
