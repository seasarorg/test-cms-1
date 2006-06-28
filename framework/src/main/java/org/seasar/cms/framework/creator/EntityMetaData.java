package org.seasar.cms.framework.creator;

public class EntityMetaData {

    private SourceCreator creator_;

    private String entityName_;

    private TypeDesc dtoTypeDesc_;

    private TypeDesc beanTypeDesc_;

    private TypeDesc daoTypeDesc_;

    private TypeDesc dxoTypeDesc_;

    public EntityMetaData(SourceCreator creator, String className) {

        creator_ = creator;
        analyze(className);
    }

    void analyze(String className) {

        TypeDesc typeDesc = new TypeDesc(className);
        entityName_ = typeDesc.getBaseName();
        dtoTypeDesc_ = new TypeDesc(creator_.getDtoPackageName() + "."
            + entityName_ + ClassDesc.KIND_DTO);
        beanTypeDesc_ = new TypeDesc(creator_.getDaoPackageName() + "."
            + entityName_);
        daoTypeDesc_ = new TypeDesc(creator_.getDaoPackageName() + "."
            + entityName_ + ClassDesc.KIND_DAO);
        dxoTypeDesc_ = new TypeDesc(creator_.getDxoPackageName() + "."
            + entityName_ + ClassDesc.KIND_DXO);
    }

    public String getEntityName() {

        return entityName_;
    }

    public TypeDesc getDtoTypeDesc() {

        return dtoTypeDesc_;
    }

    public TypeDesc getBeanTypeDesc() {

        return beanTypeDesc_;
    }

    public TypeDesc getDaoTypeDesc() {

        return daoTypeDesc_;
    }

    public TypeDesc getDxoTypeDesc() {

        return dxoTypeDesc_;
    }
}
