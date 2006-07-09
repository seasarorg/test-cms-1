package org.seasar.cms.ymir.container;

import org.seasar.framework.container.impl.SimpleComponentDef;

public class ThreadLocalComponentDef extends SimpleComponentDef {

    private ThreadLocal component_ = new ThreadLocal();

    public ThreadLocalComponentDef(Class componentClass) {
        super(componentClass);
    }

    public ThreadLocalComponentDef(Class componentClass, String componentName) {
        super(componentClass, componentName);
    }

    public Object getComponent() {

        return component_.get();
    }

    public void setComponent(Object component) {

        component_.set(component);
    }
}
