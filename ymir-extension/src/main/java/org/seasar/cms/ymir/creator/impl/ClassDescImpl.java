package org.seasar.cms.ymir.creator.impl;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.seasar.cms.ymir.creator.AbstractClassDesc;
import org.seasar.cms.ymir.creator.ClassDesc;
import org.seasar.cms.ymir.creator.MethodDesc;
import org.seasar.cms.ymir.creator.MethodDescKey;
import org.seasar.cms.ymir.creator.PropertyDesc;
import org.seasar.cms.ymir.creator.TypeDesc;

public class ClassDescImpl extends AbstractClassDesc {

    private String name_;

    private String superclassName_;

    private Map<String, PropertyDesc> propertyDescMap_ = new LinkedHashMap<String, PropertyDesc>();

    private Map<MethodDescKey, MethodDesc> methodDescMap_ = new LinkedHashMap<MethodDescKey, MethodDesc>();

    public ClassDescImpl(String name) {

        setName(name);
    }

    public Object clone() {

        ClassDescImpl cloned = (ClassDescImpl) super.clone();

        cloned.propertyDescMap_ = new LinkedHashMap<String, PropertyDesc>();
        for (Iterator itr = propertyDescMap_.entrySet().iterator(); itr
            .hasNext();) {
            Map.Entry entry = (Map.Entry) itr.next();
            cloned.propertyDescMap_.put((String) entry.getKey(),
                (PropertyDesc) ((PropertyDesc) entry.getValue()).clone());
        }
        cloned.methodDescMap_ = new LinkedHashMap<MethodDescKey, MethodDesc>();
        for (Iterator itr = methodDescMap_.entrySet().iterator(); itr.hasNext();) {
            Map.Entry entry = (Map.Entry) itr.next();
            cloned.methodDescMap_.put((MethodDescKey) entry.getKey(),
                (MethodDesc) ((MethodDesc) entry.getValue()).clone());
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

        return (PropertyDesc) propertyDescMap_.get(name);
    }

    public void setPropertyDesc(PropertyDesc propertyDesc) {

        propertyDescMap_.put(propertyDesc.getName(), propertyDesc);
    }

    public PropertyDesc[] getPropertyDescs() {

        return (PropertyDesc[]) propertyDescMap_.values().toArray(
            new PropertyDesc[0]);
    }

    public MethodDesc getMethodDesc(MethodDesc methodDesc) {

        return (MethodDesc) methodDescMap_.get(new MethodDescKey(methodDesc));
    }

    public void setMethodDesc(MethodDesc methodDesc) {

        methodDescMap_.put(new MethodDescKey(methodDesc), methodDesc);
    }

    public MethodDesc[] getMethodDescs() {

        return (MethodDesc[]) methodDescMap_.values()
            .toArray(new MethodDesc[0]);
    }

    public String getSuperclassName() {

        return superclassName_;
    }

    public void setSuperclassName(String superclassName) {

        superclassName_ = superclassName;
    }

    public void merge(ClassDesc classDesc, boolean mergeMethod) {

        if (classDesc == null) {
            return;
        }

        setSuperclassName(classDesc.getSuperclassName());

        PropertyDesc[] pds = classDesc.getPropertyDescs();
        for (int i = 0; i < pds.length; i++) {
            PropertyDesc pd = getPropertyDesc(pds[i].getName());
            if (pd == null) {
                if (mergeMethod) {
                    setPropertyDesc((PropertyDesc) pds[i].clone());
                }
            } else {
                TypeDesc td = pd.getTypeDesc();
                if (!td.isExplicit()) {
                    td.transcript(pds[i].getTypeDesc());
                }
                if (mergeMethod) {
                    pd.addMode(pds[i].getMode());
                }
            }
        }

        MethodDesc[] mds = classDesc.getMethodDescs();
        for (int i = 0; i < mds.length; i++) {
            MethodDesc md = getMethodDesc(mds[i]);
            if (md == null) {
                if (mergeMethod) {
                    setMethodDesc((MethodDesc) mds[i].clone());
                }
            } else {
                TypeDesc returnTypeDesc = md.getReturnTypeDesc();
                TypeDesc returnTypeDesc2 = mds[i].getReturnTypeDesc();
                if (!returnTypeDesc.isExplicit()
                    && !returnTypeDesc.equals(returnTypeDesc2)) {
                    returnTypeDesc.transcript(returnTypeDesc2);
                    if (mergeMethod) {
                        md.setBodyDesc(mds[i].getBodyDesc());
                    }
                }
            }
        }
    }

    public boolean isEmpty() {

        return (propertyDescMap_.isEmpty() && methodDescMap_.isEmpty());
    }
}
