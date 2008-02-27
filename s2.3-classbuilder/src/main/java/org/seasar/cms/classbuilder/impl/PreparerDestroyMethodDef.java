package org.seasar.cms.classbuilder.impl;

import java.lang.reflect.Method;

import org.seasar.cms.classbuilder.PreparerMethodDef;
import org.seasar.framework.container.impl.DestroyMethodDefImpl;

public class PreparerDestroyMethodDef extends DestroyMethodDefImpl implements
        PreparerMethodDef {
    private Method method_;

    public PreparerDestroyMethodDef(Method method) {
        super(method);
        method_ = method;
    }

    public Method getMethod() {
        return method_;
    }
}
