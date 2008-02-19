package org.seasar.cms.pluggable;

public interface ThreadContext {

    void register(ThreadLocalComponentDef componentDef);

    Object getComponent(Object key);

    Object findComponent(Object key);

    void setComponent(Object key, Object component);

    void destroy();
}
