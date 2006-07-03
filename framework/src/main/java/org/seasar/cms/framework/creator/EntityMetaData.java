package org.seasar.cms.framework.creator;

import org.seasar.cms.framework.creator.impl.SimpleClassDesc;

public class EntityMetaData {

    private SourceCreator creator_;

    private String entityName_;

    private ClassDesc dtoClassDesc_;

    private ClassDesc beanClassDesc_;

    private ClassDesc daoClassDesc_;

    private ClassDesc dxoClassDesc_;

    public EntityMetaData(SourceCreator creator, String className) {

        creator_ = creator;
        analyze(className);
    }

    void analyze(String className) {

        ClassDesc classDesc = new SimpleClassDesc(className);
        entityName_ = classDesc.getBaseName();
        dtoClassDesc_ = new SimpleClassDesc(creator_.getDtoPackageName() + "."
            + entityName_ + ClassDesc.KIND_DTO);
        beanClassDesc_ = new SimpleClassDesc(creator_.getDaoPackageName() + "."
            + entityName_);
        daoClassDesc_ = new SimpleClassDesc(creator_.getDaoPackageName() + "."
            + entityName_ + ClassDesc.KIND_DAO);
        dxoClassDesc_ = new SimpleClassDesc(creator_.getDxoPackageName() + "."
            + entityName_ + ClassDesc.KIND_DXO);
    }

    public String getEntityName() {

        return entityName_;
    }

    public ClassDesc getDtoClassDesc() {

        return dtoClassDesc_;
    }

    public ClassDesc getBeanClassDesc() {

        return beanClassDesc_;
    }

    public ClassDesc getDaoClassDesc() {

        return daoClassDesc_;
    }

    public ClassDesc getDxoClassDesc() {

        return dxoClassDesc_;
    }
}
