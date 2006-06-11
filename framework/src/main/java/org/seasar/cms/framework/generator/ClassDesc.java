package org.seasar.cms.framework.generator;

import java.util.LinkedHashMap;
import java.util.Map;

public class ClassDesc {

    private String name_;

    private Map propertyMap_ = new LinkedHashMap();

    public ClassDesc(String name) {
        setName(name);
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

    public String getPackageName() {

        int dot = name_.lastIndexOf('.');
        if (dot < 0) {
            return "";
        } else {
            return name_.substring(0, dot);
        }
    }

    public void addProperty(String name, int mode) {

        PropertyDesc descriptor = getPropertyDesc(name);
        if (descriptor == null) {
            descriptor = new PropertyDesc(name);
            propertyMap_.put(name, descriptor);
        }
        descriptor.addMode(mode);
    }

    public PropertyDesc getPropertyDesc(String name) {

        return (PropertyDesc) propertyMap_.get(name);
    }

    public void setPropertyDesc(PropertyDesc propertyDesc) {

        propertyMap_.put(propertyDesc.getName(), propertyDesc);
    }

    public PropertyDesc[] getPropertyDescs() {

        return (PropertyDesc[]) propertyMap_.values().toArray(
            new PropertyDesc[0]);
    }
}
