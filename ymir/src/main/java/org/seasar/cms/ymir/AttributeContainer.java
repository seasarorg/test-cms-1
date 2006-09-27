package org.seasar.cms.ymir;

import java.util.Enumeration;

public interface AttributeContainer {

    Object getAttribute(String name);

    Enumeration getAttributeNames();

    void setAttribute(String name, Object value);

    void removeAttribute(String name);
}
