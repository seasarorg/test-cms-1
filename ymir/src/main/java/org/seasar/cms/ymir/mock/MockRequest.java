package org.seasar.cms.ymir.mock;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.seasar.cms.ymir.AttributeContainer;
import org.seasar.cms.ymir.FormFile;
import org.seasar.cms.ymir.Request;

public class MockRequest implements Request {

    private String absolutePath_;

    private String actionName_;

    private AttributeContainer attributeContainer_;

    private String componentName_;

    private String contextPath_;

    private Object defaultReturnValue_;

    private String dispatcher_;

    private String method_;

    private Map parameterMap_;

    private String path_;

    private String pathInfo_;

    private boolean dispatchingByRequestParameter_;

    private boolean dispatchingByParameter_;

    private Locale locale_;

    private boolean matched_;

    public String getAbsolutePath() {
        return absolutePath_;
    }

    public String getActionName() {
        return actionName_;
    }

    public AttributeContainer getAttributeContainer() {
        return attributeContainer_;
    }

    public String getComponentName() {
        return componentName_;
    }

    public String getContextPath() {
        return contextPath_;
    }

    public Object getDefaultReturnValue() {
        return defaultReturnValue_;
    }

    public String getDispatcher() {
        return dispatcher_;
    }

    public boolean isDispatchingByParameter() {
        return dispatchingByParameter_;
    }

    public boolean isDispatchingByRequestParameter() {
        return dispatchingByRequestParameter_;
    }

    public boolean isMatched() {
        return matched_;
    }

    public String getMethod() {
        return method_;
    }

    public Map getParameterMap() {
        return parameterMap_;
    }

    public String getPath() {
        return path_;
    }

    public String getPathInfo() {
        return pathInfo_;
    }

    public MockRequest setAbsolutePath(String absolutePath) {
        absolutePath_ = absolutePath;
        return this;
    }

    public MockRequest setActionName(String actionName) {
        actionName_ = actionName;
        return this;
    }

    public MockRequest setAttributeContainer(
            AttributeContainer attributeContainer) {
        attributeContainer_ = attributeContainer;
        return this;
    }

    public MockRequest setComponentName(String componentName) {
        componentName_ = componentName;
        return this;
    }

    public MockRequest setContextPath(String contextPath) {
        contextPath_ = contextPath;
        return this;
    }

    public MockRequest setDefaultReturnValue(Object defaultReturnValue) {
        defaultReturnValue_ = defaultReturnValue;
        return this;
    }

    public MockRequest setDispatcher(String dispatcher) {
        dispatcher_ = dispatcher;
        return this;
    }

    public MockRequest setDispatchingByParameter(boolean dispatchingByParameter) {
        dispatchingByParameter_ = dispatchingByParameter;
        return this;
    }

    public MockRequest setDispatchingByRequestParameter(
            boolean dispatchingByRequestParameter) {
        dispatchingByRequestParameter_ = dispatchingByRequestParameter;
        return this;
    }

    public MockRequest setMatched(boolean matched) {
        matched_ = matched;
        return this;
    }

    public MockRequest setMethod(String method) {
        method_ = method;
        return this;
    }

    public MockRequest setParameterMap(Map parameterMap) {
        parameterMap_ = parameterMap;
        return this;
    }

    public MockRequest setPath(String path) {
        path_ = path;
        return this;
    }

    public MockRequest setPathInfo(String pathInfo) {
        pathInfo_ = pathInfo;
        return this;
    }

    public String extractParameterName(String name) {
        return null;
    }

    public FormFile getFileParameter(String name) {
        return null;
    }

    public Map getFileParameterMap() {
        return null;
    }

    public Iterator getFileParameterNames() {
        return null;
    }

    public FormFile[] getFileParameterValues(String name) {
        return null;
    }

    public String getParameter(String name) {
        return null;
    }

    public String getParameter(String name, String defaultValue) {
        return null;
    }

    public Iterator getParameterNames() {
        return null;
    }

    public String[] getParameterValues(String name) {
        return null;
    }

    public String[] getParameterValues(String name, String[] defaultValues) {
        return null;
    }

    public Object getAttribute(String name) {
        return null;
    }

    public Enumeration getAttributeNames() {
        return null;
    }

    public void removeAttribute(String name) {
    }

    public void setAttribute(String name, Object value) {
    }

    public Locale getLocale() {
        return locale_;
    }

    public void setLocale(Locale locale) {
        locale_ = locale;
    }
}
