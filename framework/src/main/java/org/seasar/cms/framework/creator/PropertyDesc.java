package org.seasar.cms.framework.creator;

public class PropertyDesc implements Cloneable {

    public static final int NONE = 0;

    public static final int READ = 1;

    public static final int WRITE = 2;

    private String name_;

    private TypeDesc typeDesc_ = new TypeDesc(String.class.getName());

    private int mode_;

    public PropertyDesc(String name) {

        name_ = name;
    }

    public Object clone() {

        PropertyDesc cloned;
        try {
            cloned = (PropertyDesc) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
        if (typeDesc_ != null) {
            cloned.typeDesc_ = (TypeDesc) typeDesc_.clone();
        }

        return cloned;
    }

    public String getName() {

        return name_;
    }

    public TypeDesc getTypeDesc() {

        return typeDesc_;
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
}
