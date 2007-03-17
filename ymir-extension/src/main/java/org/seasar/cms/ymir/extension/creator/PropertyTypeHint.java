package org.seasar.cms.ymir.extension.creator;

public class PropertyTypeHint {

    private String className_;

    private String propertyName_;

    private String typeName_;

    private boolean array_;

    public PropertyTypeHint(String className, String propertyName,
            String typeName, boolean array) {
        className_ = className;
        propertyName_ = propertyName;
        typeName_ = typeName;
        array_ = array;
    }

    @Override
    public String toString() {
        return "className=" + className_ + ", propertyName=" + propertyName_
                + ", typeName=" + typeName_ + ", array=" + array_;
    }

    public boolean isArray() {
        return array_;
    }

    public String getClassName() {
        return className_;
    }

    public String getPropertyName() {
        return propertyName_;
    }

    public String getTypeName() {
        return typeName_;
    }
}
