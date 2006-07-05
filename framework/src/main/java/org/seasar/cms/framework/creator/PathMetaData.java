package org.seasar.cms.framework.creator;

import java.io.File;

public interface PathMetaData {

    String getMethod();

    String getPath();

    boolean isDenied();

    String getComponentName();

    String getClassName();

    String getActionName();

    String getDefaultPath();

    File getSourceFile();

    File getTemplateFile();
}
