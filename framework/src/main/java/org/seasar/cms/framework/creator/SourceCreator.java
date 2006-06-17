package org.seasar.cms.framework.creator;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;

public interface SourceCreator {

    String getDtoPackageName();

    String getComponentName(String path, String method);

    String getActionName(String path, String method);

    String getClassName(String componentName);

    Response update(String path, String method, Request request);
}
