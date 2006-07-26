package org.seasar.cms.ymir.container;

public interface ThreadContextComponentDefFactory {

    ThreadContextComponentDef newInstance();

    String getComponentName();

    Class getComponentClass();
}
