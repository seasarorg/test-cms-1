package org.seasar.cms.ymir.impl;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.seasar.cms.ymir.AttributeContainer;
import org.seasar.cms.ymir.FormFile;
import org.seasar.cms.ymir.MatchedPathMapping;
import org.seasar.cms.ymir.Request;

public class RequestImpl implements Request {

    private String contextPath_;

    private String path_;

    private String method_;

    private String dispatcher_;

    private Map parameterMap_;

    private Map fileParameterMap_;

    private AttributeContainer attributeContainer_;

    private Locale locale_;

    private MatchedPathMapping matched_;

    public RequestImpl() {
    }

    public RequestImpl(String contextPath, String path, String method,
            String dispatcher, Map parameterMap, Map fileParameterMap,
            AttributeContainer attributeContainer, Locale locale,
            MatchedPathMapping matched) {

        contextPath_ = contextPath;
        path_ = path;
        method_ = method;
        dispatcher_ = dispatcher;
        parameterMap_ = parameterMap;
        fileParameterMap_ = fileParameterMap;
        attributeContainer_ = attributeContainer;
        locale_ = locale;
        matched_ = matched;
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

        return matched_.getComponentName();
    }

    public String getActionName() {

        return matched_.getActionName();
    }

    public String getPathInfo() {

        return matched_.getPathInfo();
    }

    public Object getDefaultReturnValue() {

        return matched_.getDefaultReturnValue();
    }

    public String extractParameterName(String name) {

        return matched_.extractParameterName(name);
    }

    public boolean isDispatchingByParameter() {

        return matched_.isDispatchingByParameter();
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

    public Locale getLocale() {
        return locale_;
    }

    public boolean isMatched() {
        return (matched_ != null);
    }

    public boolean isDenied() {
        return (matched_ != null && matched_.isDenied());
    }
}
