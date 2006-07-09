package org.seasar.cms.ymir.creator;

public interface PropertyDesc extends Cloneable {

    int NONE = 0;

    int READ = 1;

    int WRITE = 2;

    Object clone();

    String getName();

    TypeDesc getTypeDesc();

    void setTypeDesc(TypeDesc typeDesc);

    void setTypeDesc(String typeName);

    void setTypeDesc(String typeName, boolean explicit);

    int getMode();

    void setMode(int mode);

    void addMode(int mode);

    boolean isReadable();

    boolean isWritable();
}
