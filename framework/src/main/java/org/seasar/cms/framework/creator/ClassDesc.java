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

    String getKind();

    String getBaseName();
}
