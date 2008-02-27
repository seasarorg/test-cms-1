package org.seasar.cms.classbuilder.impl;

import java.lang.reflect.Method;

import org.seasar.cms.classbuilder.PreparerMethodDef;
import org.seasar.framework.container.impl.InitMethodDefImpl;

public class PreparerInitMethodDef extends InitMethodDefImpl implements
        PreparerMethodDef {
    private Method method_;

    public PreparerInitMethodDef(Method method) {
        super();
        method_ = method;
    }

    public Method getMethod() {
        return method_;
    }
}
