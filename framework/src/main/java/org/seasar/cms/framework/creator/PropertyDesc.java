package org.seasar.cms.framework.creator;

public interface PropertyDesc extends Cloneable {

    int NONE = 0;

    int READ = 1;

    int WRITE = 2;

    Object clone();

    String getName();

    TypeDesc getTypeDesc();

    void setTypeDesc(TypeDesc typeDesc);

    int getMode();

    void setMode(int mode);

    void addMode(int mode);

    boolean isReadable();

    boolean isWritable();
}
