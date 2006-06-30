package org.seasar.cms.framework.creator;

import java.util.HashMap;
import java.util.Map;

public class PropertyHolder {

    private String name_;

    private String className_;

    private Map propertyMap_ = new HashMap();

    public PropertyHolder(String name) {

        name_ = name;
    }

    public String getName() {

        return name_;
    }

    public String getClassName() {

        return className_;
    }

    public void setClassName(String className) {

        className_ = className;
    }

    public Property getProperty(String name) {

        return (Property) propertyMap_.get(name);
    }

    public void addProperty(Property property) {

        propertyMap_.put(property.getName(), property);
    }

    public static class Property {
        private String name_;

        private PropertyHolder type_;

        private boolean array_;

        public Property(String name) {
            name_ = name;
        }

        public PropertyHolder getType() {
            return type_;
        }

        public void setType(PropertyHolder type) {
            type_ = type;
        }

        public String getName() {
            return name_;
        }

        public boolean isArray() {
            return array_;
        }

        public void setArray(boolean array) {
            array_ = array;
        }
    }
}
