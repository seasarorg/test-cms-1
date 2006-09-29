package org.seasar.cms.ymir.mock;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import org.seasar.cms.ymir.AttributeContainer;
import org.seasar.cms.ymir.FormFile;
import org.seasar.cms.ymir.Request;

public class MockRequest implements Request {

    public String getAbsolutePath() {
        return null;
    }

    public String getActionName() {
        return null;
    }

    public AttributeContainer getAttributeContainer() {
        return null;
    }

    public String getComponentName() {
        return null;
    }

    public String getContextPath() {
        return null;
    }

    public Object getDefaultReturnValue() {
        return null;
    }

    public String getDispatcher() {
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

    public String getMethod() {
        return null;
    }

    public String getParameter(String name) {
        return null;
    }

    public String getParameter(String name, String defaultValue) {
        return null;
    }

    public Map getParameterMap() {
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

    public String getPath() {
        return null;
    }

    public String getPathInfo() {
        return null;
    }

    public void setActionName(String actionName) {
    }

    public void setAttributeContainer(AttributeContainer attributeContainer) {
    }

    public void setComponentName(String componentName) {
    }

    public void setContextPath(String contextPath) {
    }

    public void setDefaultReturnValue(Object defaultReturnValue) {
    }

    public void setDispatcher(String dispatcher) {
    }

    public void setFileParameterMap(Map parameterMap) {
    }

    public void setMethod(String method) {
    }

    public void setParameterMap(Map parameterMap) {
    }

    public void setPath(String path) {
    }

    public void setPathInfo(String pathInfo) {
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
}
