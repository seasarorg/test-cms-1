package org.seasar.cms.framework.creator;

abstract public class AbstractTypeDesc implements Cloneable {

    protected static final String ARRAY_SUFFIX = "[]";

    protected static final String PACKAGE_JAVA_LANG = "java.lang.";

    public static final String TYPE_VOID = "void";

    public static final String KIND_PAGE = "Page";

    public static final String KIND_DTO = "Dto";

    public static final String KIND_DAO = "Dao";

    public static final String KIND_DXO = "Dxo";

    public static final String KIND_BEAN = "Bean";

    private static final String[] AUTODETECTED_KINDS = new String[] {
        KIND_PAGE, KIND_DTO, KIND_DAO, KIND_DXO };

    abstract public String getName();

    public Object clone() {

        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getKind() {

        return getKind(getName());
    }

    String getKind(String name) {

        name = getComponentName(name);
        for (int i = 0; i < AUTODETECTED_KINDS.length; i++) {
            if (name.endsWith(AUTODETECTED_KINDS[i])) {
                return AUTODETECTED_KINDS[i];
            }
        }
        return KIND_BEAN;
    }

    public String getComponentName() {

        return getComponentName(getName());
    }

    String getComponentName(String name) {

        if (name == null) {
            return null;
        } else if (name.endsWith(ARRAY_SUFFIX)) {
            return name.substring(0, name.length() - ARRAY_SUFFIX.length());
        } else {
            return name;
        }
    }

    public String toString() {

        return "name=" + getName();
    }

    public String getShortName() {

        return getShortName(getName());
    }

    String getShortName(String name) {

        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return name;
        } else {
            return name.substring(dot + 1);
        }
    }

    public String getBaseName() {

        return getBaseName(getName());
    }

    String getBaseName(String name) {

        name = getShortName(getComponentName(name));
        for (int i = 0; i < AUTODETECTED_KINDS.length; i++) {
            if (name.endsWith(AUTODETECTED_KINDS[i])) {
                return name.substring(0, name.length()
                    - AUTODETECTED_KINDS[i].length());
            }
        }
        return name;
    }

    public String getPackageName() {

        return getPackageName(getName());
    }

    String getPackageName(String name) {

        name = getComponentName(name);
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return "";
        } else {
            return name.substring(0, dot);
        }
    }

    public String getInstanceName() {

        return getInstanceName(getName());
    }

    String getInstanceName(String name) {

        String shortName = getShortName(name);
        String componentName = getComponentName(shortName);
        if (shortName.equals(componentName)) {
            return uncapFirst(shortName);
        } else {
            return uncapFirst(componentName) + "s";
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
}
