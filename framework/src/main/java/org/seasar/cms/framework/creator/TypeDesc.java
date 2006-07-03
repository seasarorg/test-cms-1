package org.seasar.cms.framework.creator;

public interface TypeDesc extends Cloneable {

    String TYPE_VOID = "void";

    Object clone();

    ClassDesc getClassDesc();

    void setClassDesc(ClassDesc classDesc);

    boolean isArray();

    void setArray(boolean array);

    boolean isExplicit();

    void setExplicit(boolean explicit);

    void transcript(TypeDesc typeDesc);

    String getName();

    String getDefaultValue();
}
