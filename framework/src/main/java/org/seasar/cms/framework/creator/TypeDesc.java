package org.seasar.cms.framework.creator;

public class TypeDesc implements Cloneable {

    private static final String ARRAY_SUFFIX = "[]";

    private static final String PACKAGE_JAVA_LANG = "java.lang.";

    public static final String TYPE_VOID = "void";

    private String type_;

    private String defaultType_;

    private String argumentName_;

    public TypeDesc(String defaultType) {

        this(defaultType, null);
    }

    public TypeDesc(String defaultType, String argumentName) {

        setDefaultType(defaultType);
        setArgumentName(argumentName);
    }

    public Object clone() {

        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getTypeString() {

        String type;
        if (type_ != null) {
            type = type_;
        } else {
            type = defaultType_;
        }

        StringBuffer sb = new StringBuffer();
        if (type.startsWith(PACKAGE_JAVA_LANG)) {
            sb.append(type.substring(PACKAGE_JAVA_LANG.length()));
        } else {
            sb.append(type);
        }
        return sb.toString();
    }

    public String getType() {

        return type_;
    }

    public void setType(String type) {

        type_ = type;
    }

    public String getDefaultType() {

        return defaultType_;
    }

    public void setDefaultType(String defaultType) {

        defaultType_ = defaultType;
    }

    public String getArgumentNameString() {

        if (argumentName_ != null) {
            return argumentName_;
        } else {
            String typeString = getTypeString();
            int dot = typeString.lastIndexOf('.');
            if (dot >= 0) {
                typeString = typeString.substring(dot + 1);
            }
            if (typeString.endsWith(ARRAY_SUFFIX)) {
                typeString = typeString.substring(0, typeString.length()
                    - ARRAY_SUFFIX.length())
                    + "s";
            }
            return uncapFirst(typeString);
        }
    }

    String uncapFirst(String string) {

        if (string == null || string.length() == 0) {
            return string;
        } else {
            return Character.toLowerCase(string.charAt(0))
                + string.substring(1);
        }
    }

    public String getArgumentName() {

        return argumentName_;
    }

    public void setArgumentName(String argumentName) {

        argumentName_ = argumentName;
    }

    public boolean isValid() {

        if (type_ != null) {
            return isValid(type_);
        } else {
            return isValid(defaultType_);
        }
    }

    boolean isValid(String type) {

        if (type == null) {
            return false;
        }

        if (type.endsWith(ARRAY_SUFFIX)) {
            type = type.substring(0, type.length() - ARRAY_SUFFIX.length());
        }

        if (TYPE_VOID.equals(type)) {
            return true;
        } else if (isPrimitive(type)) {
            return true;
        } else {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = getClass().getClassLoader();
            }
            try {
                Class.forName(type, false, cl);
            } catch (ClassNotFoundException ex) {
                try {
                    Class.forName("java.lang." + type, false, cl);
                } catch (ClassNotFoundException ex2) {
                    return false;
                }
            }
            return true;
        }
    }

    boolean isPrimitive(String type) {

        return ("byte".equals(type) || "short".equals(type)
            || "int".equals(type) || "long".equals(type)
            || "float".equals(type) || "double".equals(type)
            || "char".equals(type) || "boolean".equals(type));
    }
}
