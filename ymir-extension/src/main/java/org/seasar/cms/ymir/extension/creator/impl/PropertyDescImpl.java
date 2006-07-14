package org.seasar.cms.ymir.extension.creator.impl;

import org.seasar.cms.ymir.extension.creator.PropertyDesc;
import org.seasar.cms.ymir.extension.creator.TypeDesc;

public class PropertyDescImpl implements PropertyDesc, Cloneable {

    public static final int NONE = 0;

    public static final int READ = 1;

    public static final int WRITE = 2;

    private String name_;

    private TypeDesc typeDesc_ = new TypeDescImpl();

    private int mode_;

    public PropertyDescImpl(String name) {

        name_ = name;
    }

    public Object clone() {

        PropertyDescImpl cloned;
        try {
            cloned = (PropertyDescImpl) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
        if (typeDesc_ != null) {
            cloned.typeDesc_ = (TypeDesc) typeDesc_.clone();
        }

        return cloned;
    }

    public String getName() {

        return name_;
    }

    public TypeDesc getTypeDesc() {

        return typeDesc_;
    }

    public void setTypeDesc(TypeDesc typeDesc) {

        typeDesc_ = typeDesc;
    }

    public void setTypeDesc(String typeName) {

        setTypeDesc(typeName, false);
    }

    public void setTypeDesc(String typeName, boolean explicit) {

        setTypeDesc(new TypeDescImpl(typeName, explicit));
    }

    public int getMode() {

        return mode_;
    }

    public void setMode(int mode) {

        mode_ = mode;
    }

    public void addMode(int mode) {

        mode_ |= mode;
    }

    public boolean isReadable() {

        return ((mode_ & READ) != 0);
    }

    public boolean isWritable() {

        return ((mode_ & WRITE) != 0);
    }
}
