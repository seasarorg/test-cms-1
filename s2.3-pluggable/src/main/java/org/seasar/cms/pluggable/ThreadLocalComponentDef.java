package org.seasar.cms.pluggable;

import org.seasar.framework.container.impl.SimpleComponentDef;

public class ThreadLocalComponentDef extends SimpleComponentDef {

    private ThreadLocal<Object> component_ = new ThreadLocal<Object>();

    public ThreadLocalComponentDef(Class<?> componentClass) {
        super(componentClass);
    }

    public ThreadLocalComponentDef(Class<?> componentClass, String componentName) {
        super(null, componentClass, componentName);
    }

    public Object getComponent() {

        return component_.get();
    }

    public void setComponent(Object component) {

        component_.set(component);
    }

    public void destroy() {
        super.destroy();
        component_ = null;
    }
}
