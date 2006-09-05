package org.seasar.cms.ymir.scope;

public interface Scope {

    Object getAttribute(String name);

    void setAttribute(String name, Object value);
}
