package org.seasar.cms.ymir.extension.creator.impl;

import org.seasar.cms.ymir.extension.creator.AbstractClassDesc;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.MethodDesc;
import org.seasar.cms.ymir.extension.creator.PropertyDesc;

public class SimpleClassDesc extends AbstractClassDesc {

    private String name_;

    public SimpleClassDesc(String name) {

        name_ = name;
    }

    public Object clone() {

        return this;
    }

    public String getName() {

        return name_;
    }

    public void setName(String name) {

        throw new UnsupportedOperationException();
    }

    public MethodDesc[] getMethodDescs() {

        return new MethodDesc[0];
    }

    public PropertyDesc[] getPropertyDescs() {

        return new PropertyDesc[0];
    }

    public MethodDesc getMethodDesc(MethodDesc methodDesc) {

        return null;
    }

    public void setPropertyDesc(PropertyDesc propertyDesc) {

        throw new UnsupportedOperationException();
    }

    public PropertyDesc addProperty(String name, int mode) {

        throw new UnsupportedOperationException();
    }

    public void merge(ClassDesc classDesc, boolean mergeMethod) {

        throw new UnsupportedOperationException();
    }

    public void setMethodDesc(MethodDesc methodDesc) {

        throw new UnsupportedOperationException();
    }

    public void setSuperclassName(String superclassName) {

        throw new UnsupportedOperationException();
    }

    public String getSuperclassName() {

        throw new UnsupportedOperationException();
    }

    public boolean isEmpty() {

        return true;
    }

    public PropertyDesc getPropertyDesc(String name) {

        return null;
    }
}
