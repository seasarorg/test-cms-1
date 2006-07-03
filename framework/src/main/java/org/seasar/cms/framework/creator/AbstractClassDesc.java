package org.seasar.cms.framework.creator;

abstract public class AbstractClassDesc implements ClassDesc {

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

        for (int i = 0; i < AUTODETECTED_KINDS.length; i++) {
            if (name.endsWith(AUTODETECTED_KINDS[i])) {
                return AUTODETECTED_KINDS[i];
            }
        }
        return KIND_BEAN;
    }

    public boolean isKindOf(String kind) {

        return kind.equals(getKind());
    }

    public String toString() {

        return getName();
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

        name = getShortName(name);
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

        return uncapFirst(getShortName(name));
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
