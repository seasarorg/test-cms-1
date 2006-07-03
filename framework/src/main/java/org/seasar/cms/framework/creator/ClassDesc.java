package org.seasar.cms.framework.creator;

public interface ClassDesc extends Cloneable {

    String KIND_PAGE = "Page";

    String KIND_DTO = "Dto";

    String KIND_DAO = "Dao";

    String KIND_DXO = "Dxo";

    String KIND_BEAN = "Bean";

    Object clone();

    String getName();

    String getPackageName();

    String getShortName();

    String getInstanceName();

    String getKind();

    boolean isKindOf(String kind);

    String getBaseName();

    MethodDesc getMethodDesc(MethodDesc methodDesc);

    MethodDesc[] getMethodDescs();

    PropertyDesc getPropertyDesc(String name);

    PropertyDesc[] getPropertyDescs();

    void setPropertyDesc(PropertyDesc propertyDesc);

    PropertyDesc addProperty(String name, int mode);

    void merge(ClassDesc classDesc, boolean mergeMethod);

    void setName(String name);

    void setMethodDesc(MethodDesc methodDesc);

    void setSuperclassName(String superclassName);

    String getSuperclassName();

    boolean isEmpty();
}
