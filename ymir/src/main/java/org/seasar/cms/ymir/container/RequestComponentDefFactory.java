package org.seasar.cms.ymir.container;

import org.seasar.cms.ymir.Request;

public class RequestComponentDefFactory implements
    ThreadContextComponentDefFactory {

    public Class getComponentClass() {
        return Request.class;
    }

    public String getComponentName() {
        return "ymirRequest";
    }

    public ThreadContextComponentDef newInstance() {
        return new ThreadContextComponentDef(getComponentClass(),
            getComponentName());
    }
}
