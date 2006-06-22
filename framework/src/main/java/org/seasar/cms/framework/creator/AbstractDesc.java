package org.seasar.cms.framework.creator;

abstract public class AbstractDesc implements Cloneable {

    private static final String PACKAGE_JAVA_LANG = "java.lang.";

    public static final String TYPE_VOID = "void";

    protected Object clone() {

        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected String getTypeString(String first, String second, String third) {

        String type;
        if (first != null) {
            type = first;
        } else if (second != null) {
            type = second;
        } else {
            type = third;
        }

        StringBuffer sb = new StringBuffer();
        if (type.startsWith(PACKAGE_JAVA_LANG)) {
            sb.append(type.substring(PACKAGE_JAVA_LANG.length()));
        } else {
            sb.append(type);
        }
        return sb.toString();
    }
}
