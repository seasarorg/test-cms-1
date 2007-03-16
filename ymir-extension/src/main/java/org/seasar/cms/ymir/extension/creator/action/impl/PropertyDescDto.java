package org.seasar.cms.ymir.extension.creator.action.impl;

import org.seasar.cms.ymir.extension.creator.PropertyDesc;

public class PropertyDescDto {

    private String name_;

    private String typeName_;

    public PropertyDescDto(PropertyDesc pd) {
        name_ = pd.getName();
        typeName_ = pd.getTypeDesc().getName();
    }

    public String getName() {
        return name_;
    }

    public String getTypeName() {
        return typeName_;
    }
}
