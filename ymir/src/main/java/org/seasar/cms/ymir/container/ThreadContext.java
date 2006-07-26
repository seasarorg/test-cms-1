package org.seasar.cms.ymir.container;

public interface ThreadContext {

    void register(ThreadLocalComponentDef componentDef);

    Object getComponent(Object key);

    void setComponent(Object key, Object component);

    void destroy();
}
