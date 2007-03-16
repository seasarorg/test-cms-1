package org.seasar.cms.ymir.extension.creator.action.impl;

import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.PropertyDesc;

public class ClassDescDto {

    private String name_;

    private PropertyDescDto[] propertyDescs_;

    private boolean checked_;

    public ClassDescDto(ClassDesc classDesc, boolean checked) {
        name_ = classDesc.getName();
        PropertyDesc[] pds = classDesc.getPropertyDescs();
        propertyDescs_ = new PropertyDescDto[pds.length];
        for (int i = 0; i < pds.length; i++) {
            propertyDescs_[i] = new PropertyDescDto(pds[i]);
        }
        checked_ = checked;
    }

    public boolean isChecked() {
        return checked_;
    }

    public String getName() {
        return name_;
    }

    public PropertyDescDto[] getPropertyDescs() {
        return propertyDescs_;
    }
}
