package org.seasar.cms.ymir.extension.creator;

public interface FormDesc {

    String getActionPageClassName();

    boolean isDispatchingByRequestParameter();

    void setActionMethodDesc(String parameterName);

    ClassDesc getClassDesc();
}
