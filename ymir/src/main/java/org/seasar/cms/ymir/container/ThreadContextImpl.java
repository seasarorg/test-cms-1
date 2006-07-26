package org.seasar.cms.ymir.container;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.impl.S2ContainerImpl;

public class ThreadContextImpl implements ThreadContext {

    S2Container container_ = new S2ContainerImpl();

    public void register(ThreadLocalComponentDef componentDef) {
        container_.register(componentDef);
    }

    public Object getComponent(Object key) {
        return container_.getComponent(key);
    }

    public void setComponent(Object key, Object component) {
        ((ThreadLocalComponentDef) container_.getComponentDef(key))
            .setComponent(component);
    }

    public void destroy() {
        container_.destroy();
        container_ = null;
    }
}
