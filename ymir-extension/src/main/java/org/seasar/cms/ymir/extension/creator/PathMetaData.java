package org.seasar.cms.ymir.extension.creator;

import java.io.File;

public interface PathMetaData {

    String getMethod();

    String getPath();

    boolean isDenied();

    String getComponentName();

    String getClassName();

    String getActionName();

    File getBaseSourceFile();

    File getSourceFile();

    Template getTemplate();
}
