package org.seasar.cms.framework.impl;

import java.util.Map;

import org.seasar.cms.framework.Request;

public class RequestImpl implements Request {

    private String contextPath_;

    private String path_;

    private String method_;

    private String dispatcher_;

    private Map parameterMap_;

    private String pathInfo_;

    public RequestImpl() {
    }

    public RequestImpl(String contextPath, String path, String method,
        String dispatcher, Map parameterMap, String pathInfo) {

        contextPath_ = contextPath;
        path_ = path;
        method_ = method;
        dispatcher_ = dispatcher;
        parameterMap_ = parameterMap;
        pathInfo_ = pathInfo;
    }

    public String getDispatcher() {

        return dispatcher_;
    }

    public void setDispatcher(String dispatcher) {

        dispatcher_ = dispatcher;
    }

    public String getMethod() {

        return method_;
    }

    public void setMethod(String method) {

        method_ = method;
    }

    public String getContextPath() {

        return contextPath_;
    }

    public void setContextPath(String contextPath) {

        contextPath_ = contextPath;
    }

    public String getPath() {

        return path_;
    }

    public void setPath(String path) {

        path_ = path;
    }

    public String getAbsolutePath() {

        return contextPath_ + path_;
    }

    public Map getParameterMap() {

        return parameterMap_;
    }

    public void setParameterMap(Map parameterMap) {

        parameterMap_ = parameterMap;
    }

    public String getPathInfo() {

        return pathInfo_;
    }

    public void setPathInfo(String pathInfo) {

        pathInfo_ = pathInfo;
    }
}
