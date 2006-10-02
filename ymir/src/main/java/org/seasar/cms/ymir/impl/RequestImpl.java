package org.seasar.cms.ymir.impl;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import org.seasar.cms.ymir.AttributeContainer;
import org.seasar.cms.ymir.FormFile;
import org.seasar.cms.ymir.Request;

public class RequestImpl implements Request {

    private String contextPath_;

    private String path_;

    private String method_;

    private String dispatcher_;

    private Map parameterMap_;

    private Map fileParameterMap_;

    private AttributeContainer attributeContainer_;

    private String pathInfo_;

    private String componentName_;

    private String actionName_;

    private Object defaultReturnValue_;

    private boolean dispatchingByRequestParameter_;

    public RequestImpl() {
    }

    public RequestImpl(String contextPath, String path, String method,
            String dispatcher, Map parameterMap, Map fileParameterMap,
            AttributeContainer attributeContainer, String componentName,
            String actionName, String pathInfo, Object defaultReturnValue,
            boolean dispatchingByRequestParameter) {

        contextPath_ = contextPath;
        path_ = path;
        method_ = method;
        dispatcher_ = dispatcher;
        parameterMap_ = parameterMap;
        fileParameterMap_ = fileParameterMap;
        attributeContainer_ = attributeContainer;
        componentName_ = componentName;
        actionName_ = actionName;
        pathInfo_ = pathInfo;
        defaultReturnValue_ = defaultReturnValue;
        dispatchingByRequestParameter_ = dispatchingByRequestParameter;
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

        if ("".equals(path_)) {
            return contextPath_ + "/";
        } else {
            return contextPath_ + path_;
        }
    }

    public String getParameter(String name) {

        return getParameter(name, null);
    }

    public String getParameter(String name, String defaultValue) {

        String[] values = (String[]) parameterMap_.get(name);
        if (values != null && values.length > 0) {
            return values[0];
        } else {
            return defaultValue;
        }
    }

    public String[] getParameterValues(String name, String[] defaultValues) {

        String[] values = (String[]) parameterMap_.get(name);
        if (values != null) {
            return values;
        } else {
            return defaultValues;
        }
    }

    public String[] getParameterValues(String name) {

        return getParameterValues(name, null);
    }

    public Iterator getParameterNames() {

        return parameterMap_.keySet().iterator();
    }

    public Map getParameterMap() {

        return parameterMap_;
    }

    public void setParameterMap(Map parameterMap) {

        parameterMap_ = parameterMap;
    }

    public FormFile getFileParameter(String name) {

        FormFile[] values = (FormFile[]) fileParameterMap_.get(name);
        if (values != null && values.length > 0) {
            return values[0];
        } else {
            return null;
        }
    }

    public FormFile[] getFileParameterValues(String name) {

        FormFile[] values = (FormFile[]) fileParameterMap_.get(name);
        if (values != null) {
            return values;
        } else {
            return null;
        }
    }

    public Iterator getFileParameterNames() {

        return fileParameterMap_.keySet().iterator();
    }

    public Map getFileParameterMap() {

        return fileParameterMap_;
    }

    public void setFileParameterMap(Map fileParameterMap) {

        fileParameterMap_ = fileParameterMap;
    }

    public AttributeContainer getAttributeContainer() {

        return attributeContainer_;
    }

    public void setAttributeContainer(AttributeContainer attributeContainer) {

        attributeContainer_ = attributeContainer;
    }

    public String getComponentName() {

        return componentName_;
    }

    public void setComponentName(String componentName) {

        componentName_ = componentName;
    }

    public String getActionName() {

        return actionName_;
    }

    public void setActionName(String actionName) {

        actionName_ = actionName;
    }

    public String getPathInfo() {

        return pathInfo_;
    }

    public void setPathInfo(String pathInfo) {

        pathInfo_ = pathInfo;
    }

    public Object getDefaultReturnValue() {

        return defaultReturnValue_;
    }

    public void setDefaultReturnValue(Object defaultReturnValue) {

        defaultReturnValue_ = defaultReturnValue;
    }

    public boolean isDispatchingByRequestParameter() {

        return dispatchingByRequestParameter_;
    }

    public void setDispatchingByRequestParameter(
            boolean dispatchingByRequestParameter) {

        dispatchingByRequestParameter_ = dispatchingByRequestParameter;
    }

    public Object getAttribute(String name) {
        return attributeContainer_.getAttribute(name);
    }

    public Enumeration getAttributeNames() {
        return attributeContainer_.getAttributeNames();
    }

    public void removeAttribute(String name) {
        attributeContainer_.removeAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        attributeContainer_.setAttribute(name, value);
    }
}
