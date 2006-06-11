package org.seasar.cms.framework.generator;

public class PropertyDesc {

    private static final String PACKAGE_JAVA_LANG = "java.lang.";

    public static final int NONE = 0;

    public static final int READ = 1;

    public static final int WRITE = 2;

    public static final int ARRAY = 4;

    private String name_;

    private String type_;

    private int mode_;

    public PropertyDesc(String name) {

        name_ = name;
    }

    public String getName() {

        return name_;
    }

    public String getType() {

        return type_;
    }

    public String getTypeString() {

        if (type_ == null) {
            return "String";
        }

        StringBuffer sb = new StringBuffer();
        if (type_.startsWith(PACKAGE_JAVA_LANG)) {
            sb.append(type_.substring(PACKAGE_JAVA_LANG.length()));
        } else {
            sb.append(type_);
        }
        if (isArray()) {
            sb.append("[]");
        }
        return sb.toString();
    }

    public void setType(String type) {

        type_ = type;
    }

    public int getMode() {

        return mode_;
    }

    public void setMode(int mode) {

        mode_ = mode;
    }

    public void addMode(int mode) {

        mode_ |= mode;
    }

    public boolean isReadable() {

        return ((mode_ & READ) != 0);
    }

    public boolean isWritable() {

        return ((mode_ & WRITE) != 0);
    }

    public boolean isArray() {

        return ((mode_ & ARRAY) != 0);
    }
}
