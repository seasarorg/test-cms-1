package org.seasar.cms.ymir.impl;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.seasar.cms.ymir.AttributeContainer;

public class HttpServletRequestAttributeContainer implements AttributeContainer {

    private HttpServletRequest request_;

    public HttpServletRequestAttributeContainer(HttpServletRequest request) {
        request_ = request;
    }

    public Object getAttribute(String name) {
        return request_.getAttribute(name);
    }

    public Enumeration getAttributeNames() {
        return request_.getAttributeNames();
    }

    public void removeAttribute(String name) {
        request_.removeAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        request_.setAttribute(name, value);
    }
}
