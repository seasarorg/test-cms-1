package org.seasar.cms.framework.creator.impl;

import org.seasar.cms.framework.creator.ParameterDesc;
import org.seasar.cms.framework.creator.TypeDesc;

public class ParameterDescImpl implements ParameterDesc {

    private TypeDesc typeDesc_;

    private String name_;

    public ParameterDescImpl(TypeDesc typeDesc) {

        setTypeDesc(typeDesc);
    }

    public Object clone() {

        ParameterDescImpl cloned;
        try {
            cloned = (ParameterDescImpl) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
        if (cloned.typeDesc_ != null) {
            cloned.typeDesc_ = (TypeDesc) typeDesc_.clone();
        }
        return cloned;
    }

    public String toString() {

        return typeDesc_ + " " + name_;
    }

    public TypeDesc getTypeDesc() {

        return typeDesc_;
    }

    public void setTypeDesc(TypeDesc typeDesc) {

        typeDesc_ = typeDesc;
    }

    public String getName() {

        if (name_ != null) {
            return name_;
        } else {
            return getTypeDesc().getInstanceName();
        }
    }

    public void setName(String name) {

        name_ = name;
    }
}
