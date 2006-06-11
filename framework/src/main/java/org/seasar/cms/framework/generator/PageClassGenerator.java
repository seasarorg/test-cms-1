package org.seasar.cms.framework.generator;

public interface PageClassGenerator {

    String getDtoPackageName();

    String getComponentName(String path);

    String getClassName(String componentName);

    ClassDesc[] update(String path);
}
