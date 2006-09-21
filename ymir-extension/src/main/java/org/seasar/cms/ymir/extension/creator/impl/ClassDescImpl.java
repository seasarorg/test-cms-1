package org.seasar.cms.ymir.extension.creator.impl;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.seasar.cms.ymir.extension.creator.AbstractClassDesc;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.MethodDesc;
import org.seasar.cms.ymir.extension.creator.MethodDescKey;
import org.seasar.cms.ymir.extension.creator.PropertyDesc;
import org.seasar.cms.ymir.extension.creator.TypeDesc;

public class ClassDescImpl extends AbstractClassDesc {

    private String name_;

    private Class superclass_;

    private Map<String, PropertyDesc> propertyDescMap_ = new LinkedHashMap<String, PropertyDesc>();

    private Map<MethodDescKey, MethodDesc> methodDescMap_ = new LinkedHashMap<MethodDescKey, MethodDesc>();

    public ClassDescImpl(String name) {

        setName(name);
    }

    public Object clone() {

        ClassDescImpl cloned = (ClassDescImpl) super.clone();

        cloned.propertyDescMap_ = new LinkedHashMap<String, PropertyDesc>();
        for (Iterator<Map.Entry<String, PropertyDesc>> itr = propertyDescMap_
                .entrySet().iterator(); itr.hasNext();) {
            Map.Entry<String, PropertyDesc> entry = itr.next();
            cloned.propertyDescMap_.put(entry.getKey(), (PropertyDesc) entry
                    .getValue().clone());
        }
        cloned.methodDescMap_ = new LinkedHashMap<MethodDescKey, MethodDesc>();
        for (Iterator<Map.Entry<MethodDescKey, MethodDesc>> itr = methodDescMap_
                .entrySet().iterator(); itr.hasNext();) {
            Map.Entry<MethodDescKey, MethodDesc> entry = itr.next();
            cloned.methodDescMap_.put(entry.getKey(), (MethodDesc) entry
                    .getValue().clone());
        }
        return cloned;
    }

    public String getName() {
        return name_;
    }

    public void setName(String name) {
        name_ = name;
    }

    public PropertyDesc addProperty(String name, int mode) {

        PropertyDesc propertyDesc = getPropertyDesc(name);
        if (propertyDesc == null) {
            propertyDesc = new PropertyDescImpl(name);
            propertyDescMap_.put(name, propertyDesc);
        }
        propertyDesc.addMode(mode);
        return propertyDesc;
    }

    public PropertyDesc getPropertyDesc(String name) {

        return propertyDescMap_.get(name);
    }

    public void setPropertyDesc(PropertyDesc propertyDesc) {

        propertyDescMap_.put(propertyDesc.getName(), propertyDesc);
    }

    public void removePropertyDesc(String name) {

        propertyDescMap_.remove(name);
    }

    public PropertyDesc[] getPropertyDescs() {

        return propertyDescMap_.values().toArray(new PropertyDesc[0]);
    }

    public MethodDesc getMethodDesc(MethodDesc methodDesc) {

        return methodDescMap_.get(new MethodDescKey(methodDesc));
    }

    public void setMethodDesc(MethodDesc methodDesc) {

        methodDescMap_.put(new MethodDescKey(methodDesc), methodDesc);
    }

    public MethodDesc[] getMethodDescs() {

        return methodDescMap_.values().toArray(new MethodDesc[0]);
    }

    public void removeMethodDesc(MethodDesc methodDesc) {

        methodDescMap_.remove(new MethodDescKey(methodDesc));
    }

    public String getSuperclassName() {

        if (superclass_ != null) {
            return superclass_.getName();
        } else {
            return null;
        }
    }

    public Class getSuperclass() {

        return superclass_;
    }

    public void setSuperclass(Class superclass) {

        superclass_ = superclass;
    }

    public void merge(ClassDesc classDesc) {

        if (classDesc == null) {
            return;
        }

        if (superclass_ == null) {
            setSuperclass(classDesc.getSuperclass());
        }

        PropertyDesc[] propertyDescs = classDesc.getPropertyDescs();
        for (int i = 0; i < propertyDescs.length; i++) {
            PropertyDesc pd = getPropertyDesc(propertyDescs[i].getName());
            if (pd == null) {
                setPropertyDesc((PropertyDesc) propertyDescs[i].clone());
            } else {
                TypeDesc td = pd.getTypeDesc();
                if (!td.isExplicit()) {
                    td.transcript(propertyDescs[i].getTypeDesc());
                }
                pd.addMode(propertyDescs[i].getMode());
            }
        }

        MethodDesc[] methodDescs = classDesc.getMethodDescs();
        for (int i = 0; i < methodDescs.length; i++) {
            MethodDesc md = getMethodDesc(methodDescs[i]);
            if (md == null) {
                setMethodDesc((MethodDesc) methodDescs[i].clone());
            } else {
                TypeDesc returnTd = md.getReturnTypeDesc();
                TypeDesc returnTypeDesc = methodDescs[i].getReturnTypeDesc();
                if (!returnTd.isExplicit() && !returnTd.equals(returnTypeDesc)) {
                    returnTd.transcript(returnTypeDesc);
                    md.setBodyDesc(methodDescs[i].getBodyDesc());
                }
            }
        }
    }

    public boolean isEmpty() {

        return (propertyDescMap_.isEmpty() && methodDescMap_.isEmpty());
    }

    public void clear() {

        superclass_ = null;
        propertyDescMap_.clear();
        methodDescMap_.clear();
    }
}
