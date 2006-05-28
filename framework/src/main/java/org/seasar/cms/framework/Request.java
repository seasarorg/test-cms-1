package org.seasar.cms.framework;

import java.util.Map;

public interface Request {

    String getPath();

    void setPath(String path);

    String getMethod();

    void setMethod(String method);

    String getDispatcher();

    void setDispatcher(String dispatcher);

    Map getParameterMap();

    void setParameterMap(Map parameterMap);

    String getPathInfo();

    void setPathInfo(String pathInfo);
}
