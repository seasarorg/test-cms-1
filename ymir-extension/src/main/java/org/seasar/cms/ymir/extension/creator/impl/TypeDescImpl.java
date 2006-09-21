package org.seasar.cms.ymir.extension.creator.impl;

import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.TypeDesc;

public class TypeDescImpl implements TypeDesc {

    private static final String ARRAY_SUFFIX = "[]";

    private static final String PACKAGE_JAVA_LANG = "java.lang.";

    private static final String NULL_VALUE = "null";

    private ClassDesc classDesc_;

    private boolean array_;

    private boolean explicit_;

    private static final Map<String, String> defaultValueMap_;

    static {

        defaultValueMap_ = new HashMap<String, String>();
        defaultValueMap_.put("byte", "0");
        defaultValueMap_.put("short", "0");
        defaultValueMap_.put("int", "0");
        defaultValueMap_.put("long", "0");
        defaultValueMap_.put("float", "0");
        defaultValueMap_.put("double", "0");
        defaultValueMap_.put("char", "0");
        defaultValueMap_.put("boolean", "false");
    }

    public TypeDescImpl() {

        this(DEFAULT_CLASSDESC);
    }

    public TypeDescImpl(String typeName) {

        this(typeName, false);
    }

    public TypeDescImpl(String typeName, boolean explicit) {

        this(new SimpleClassDesc(getComponentName(typeName)),
                isArray(typeName), explicit);
    }

    public TypeDescImpl(ClassDesc classDesc) {

        this(classDesc, false);
    }

    public TypeDescImpl(ClassDesc classDesc, boolean array) {

        this(classDesc, array, false);
    }

    public TypeDescImpl(ClassDesc classDesc, boolean array, boolean explicit) {

        setClassDesc(classDesc);
        setArray(array);
        setExplicit(explicit);
    }

    public TypeDescImpl(Class clazz) {

        this(new SimpleClassDesc(clazz.getName()), clazz.isArray());
    }

    static String getComponentName(String name) {

        if (name.endsWith(ARRAY_SUFFIX)) {
            return name.substring(0, name.length() - ARRAY_SUFFIX.length());
        } else {
            return name;
        }
    }

    static boolean isArray(String name) {

        return name.endsWith(ARRAY_SUFFIX);
    }

    public Object clone() {

        TypeDescImpl cloned;
        try {
            cloned = (TypeDescImpl) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
        if (classDesc_ != null) {
            cloned.classDesc_ = new SimpleClassDesc(classDesc_.getName());
        }
        return cloned;
    }

    public boolean equals(Object obj) {

        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        TypeDescImpl o = (TypeDescImpl) obj;
        if (!o.classDesc_.getName().equals(classDesc_.getName())) {
            return false;
        }
        if (o.array_ != array_) {
            return false;
        }
        return true;
    }

    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append(classDesc_.getName());
        if (array_) {
            sb.append(ARRAY_SUFFIX);
        }
        return sb.toString();
    }

    public ClassDesc getClassDesc() {

        return classDesc_;
    }

    public void setClassDesc(ClassDesc classDesc) {

        classDesc_ = classDesc;
    }

    public void setClassDesc(String className) {

        setClassDesc(new SimpleClassDesc(className));
    }

    public boolean isArray() {

        return array_;
    }

    public void setArray(boolean array) {

        array_ = array;
    }

    public boolean isExplicit() {

        return explicit_;
    }

    public void setExplicit(boolean explicit) {

        explicit_ = explicit;
    }

    public void transcript(TypeDesc typeDesc) {

        setClassDesc(typeDesc.getClassDesc());
        setArray(typeDesc.isArray());
    }

    public String getName() {

        String name = classDesc_.getName();
        StringBuffer sb = new StringBuffer();
        if (name.startsWith(PACKAGE_JAVA_LANG)) {
            sb.append(name.substring(PACKAGE_JAVA_LANG.length()));
        } else {
            sb.append(name);
        }
        if (array_) {
            sb.append(ARRAY_SUFFIX);
        }
        return sb.toString();
    }

    public String getDefaultValue() {

        if (array_) {
            return NULL_VALUE;
        } else {
            String name = classDesc_.getName();
            String value = (String) defaultValueMap_.get(name);
            if (value != null) {
                return value;
            } else {
                return NULL_VALUE;
            }
        }
    }

    public String getInstanceName() {

        if (array_) {
            return classDesc_.getInstanceName() + "s";
        } else {
            return classDesc_.getInstanceName();
        }
    }
}
