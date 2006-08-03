package org.seasar.cms.pluggable;

public interface ThreadContextComponentDefFactory {

    ThreadContextComponentDef newInstance();

    String getComponentName();

    Class getComponentClass();
}
