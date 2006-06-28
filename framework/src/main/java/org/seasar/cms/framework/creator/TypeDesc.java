package org.seasar.cms.framework.creator;

public class TypeDesc extends AbstractTypeDesc {

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

        return super.clone();
    }

    public String getName() {

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

    public String getArgumentName() {

        if (argumentName_ != null) {
            return argumentName_;
        } else {
            return getInstanceName();
        }
    }

    public void setArgumentName(String argumentName) {

        argumentName_ = argumentName;
    }

    public boolean isArray() {

        return getName().endsWith(ARRAY_SUFFIX);
    }
}
