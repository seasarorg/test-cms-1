package org.seasar.cms.framework.creator;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClassDesc implements Cloneable {

    public static final String KIND_PAGE = "Page";

    public static final String KIND_DTO = "Dto";

    public static final String KIND_DAO = "Dao";

    public static final String KIND_BEAN = "Bean";

    private static final String[] AUTODETECTED_KINDS = new String[] {
        KIND_PAGE, KIND_DTO, KIND_DAO };

    private String name_;

    private String superclassName_;

    private Map propertyDescMap_ = new LinkedHashMap();

    private Map methodDescMap_ = new LinkedHashMap();

    private String kind_ = KIND_BEAN;

    public ClassDesc(String name) {

        setName(name);
        for (int i = 0; i < AUTODETECTED_KINDS.length; i++) {
            if (name.endsWith(AUTODETECTED_KINDS[i])) {
                setKind(AUTODETECTED_KINDS[i]);
                break;
            }
        }
    }

    public Object clone() {

        ClassDesc cloned;
        try {
            cloned = (ClassDesc) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
        cloned.propertyDescMap_ = new LinkedHashMap();
        for (Iterator itr = propertyDescMap_.entrySet().iterator(); itr
            .hasNext();) {
            Map.Entry entry = (Map.Entry) itr.next();
            cloned.propertyDescMap_.put(entry.getKey(), ((PropertyDesc) entry
                .getValue()).clone());
        }
        cloned.methodDescMap_ = new LinkedHashMap();
        for (Iterator itr = methodDescMap_.entrySet().iterator(); itr.hasNext();) {
            Map.Entry entry = (Map.Entry) itr.next();
            cloned.methodDescMap_.put(entry.getKey(), ((MethodDesc) entry
                .getValue()).clone());
        }
        return cloned;
    }

    public String toString() {
        return "name=" + name_;
    }

    public String getName() {
        return name_;
    }

    public void setName(String name) {
        name_ = name;
    }

    public String getShortName() {

        int dot = name_.lastIndexOf('.');
        if (dot < 0) {
            return name_;
        } else {
            return name_.substring(dot + 1);
        }
    }

    public String getBaseName() {

        String shortName = getShortName();
        if (shortName.endsWith(kind_)) {
            return shortName.substring(0, shortName.length() - kind_.length());
        } else {
            return shortName;
        }
    }

    public String getPackageName() {

        int dot = name_.lastIndexOf('.');
        if (dot < 0) {
            return "";
        } else {
            return name_.substring(0, dot);
        }
    }

    public PropertyDesc addProperty(String name, int mode) {

        PropertyDesc propertyDesc = getPropertyDesc(name);
        if (propertyDesc == null) {
            propertyDesc = new PropertyDesc(name);
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

    public String getKind() {
        return kind_;
    }

    public void setKind(String kind) {
        kind_ = kind;
    }

    public MethodDesc getMethodDesc(String name) {

        return (MethodDesc) methodDescMap_.get(name);
    }

    public void setMethodDesc(MethodDesc methodDesc) {

        methodDescMap_.put(methodDesc.getName(), methodDesc);
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

    public ClassDesc merge(ClassDesc classDesc) {

        if (classDesc == null) {
            return this;
        }

        setSuperclassName(classDesc.getSuperclassName());

        PropertyDesc[] pds = classDesc.getPropertyDescs();
        for (int i = 0; i < pds.length; i++) {
            PropertyDesc pd = getPropertyDesc(pds[i].getName());
            if (pd == null) {
                setPropertyDesc((PropertyDesc) pds[i].clone());
            } else {
                if (pd.getType() == null) {
                    pd.setType(pds[i].getType());
                }
                pd.addMode(pds[i].getMode());
            }
        }

        MethodDesc[] mds = classDesc.getMethodDescs();
        for (int i = 0; i < mds.length; i++) {
            MethodDesc md = getMethodDesc(mds[i].getName());
            if (md == null) {
                setMethodDesc((MethodDesc) mds[i].clone());
            } else {
                if (md.getReturnType() == null) {
                    md.setReturnType(mds[i].getReturnType());
                }
                md.setBody(mds[i].getBody());
            }
        }

        return this;
    }
}
