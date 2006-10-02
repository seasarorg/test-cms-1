package org.seasar.cms.ymir.extension.creator.impl;

import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.FormDesc;

public class FormDescImpl implements FormDesc {

    private ClassDesc classDesc_;

    private String actionName_;

    private boolean dispatchingByRequestParameter_;

    public FormDescImpl(ClassDesc classDesc, String actionName,
            boolean dispatchingByRequestParameter) {

        classDesc_ = classDesc;
        actionName_ = actionName;
        dispatchingByRequestParameter_ = dispatchingByRequestParameter;
    }

    public void setActionMethodDesc(String parameterName) {

        if (parameterName == null) {
            return;
        }
        classDesc_.setMethodDesc(new MethodDescImpl(
                (actionName_ + "_" + parameterName)));
    }

    public String getActionPageClassName() {

        return classDesc_.getName();
    }

    public boolean isDispatchingByRequestParameter() {

        return dispatchingByRequestParameter_;
    }

    public ClassDesc getClassDesc() {

        return classDesc_;
    }
}
