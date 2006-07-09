package org.seasar.cms.ymir;

import java.util.Iterator;
import java.util.Map;

public interface Request {

    String METHOD_CONNECT = "CONNECT";

    String METHOD_DELETE = "DELETE";

    String METHOD_GET = "GET";

    String METHOD_HEAD = "HEAD";

    String METHOD_LINK = "LINK";

    String METHOD_OPTIONS = "OPTIONS";

    String METHOD_PATCH = "PATCH";

    String METHOD_POST = "POST";

    String METHOD_PUT = "PUT";

    String METHOD_TRACE = "TRACE";

    String METHOD_UNLINK = "UNLINK";

    String DISPATCHER_REQUEST = "REQUEST";

    String DISPATCHER_FORWARD = "FORWARD";

    String DISPATCHER_INCLUDE = "INCLUDE";

    String DISPATCHER_ERROR = "ERROR";

    String getContextPath();

    void setContextPath(String contextPath);

    String getPath();

    void setPath(String path);

    String getAbsolutePath();

    String getMethod();

    void setMethod(String method);

    String getDispatcher();

    void setDispatcher(String dispatcher);

    String getParameter(String name);

    String getParameter(String name, String defaultValue);

    String[] getParameterValues(String name);

    String[] getParameterValues(String name, String[] defaultValues);

    Iterator getParameterNames();

    Map getParameterMap();

    void setParameterMap(Map parameterMap);

    FormFile getFileParameter(String name);

    FormFile[] getFileParameterValues(String name);

    Iterator getFileParameterNames();

    Map getFileParameterMap();

    void setFileParameterMap(Map parameterMap);

    String getPathInfo();

    void setPathInfo(String pathInfo);
}
