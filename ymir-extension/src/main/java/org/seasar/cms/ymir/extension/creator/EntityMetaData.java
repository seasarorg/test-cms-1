package org.seasar.cms.ymir.extension.creator;

import org.seasar.cms.ymir.extension.creator.impl.SimpleClassDesc;

public class EntityMetaData {

    private SourceCreator creator_;

    private String entityName_;

    private String dtoClassName_;

    private String beanClassName_;

    private String daoClassName_;

    private String dxoClassName_;

    public EntityMetaData(SourceCreator creator, String className) {

        creator_ = creator;
        analyze(className);
    }

    void analyze(String className) {

        entityName_ = new SimpleClassDesc(className).getBaseName();
        dtoClassName_ = creator_.getDtoPackageName() + "." + entityName_
                + ClassDesc.KIND_DTO;
        beanClassName_ = creator_.getDaoPackageName() + "." + entityName_;
        daoClassName_ = creator_.getDaoPackageName() + "." + entityName_
                + ClassDesc.KIND_DAO;
        dxoClassName_ = creator_.getDxoPackageName() + "." + entityName_
                + ClassDesc.KIND_DXO;
    }

    public String getEntityName() {

        return entityName_;
    }

    public ClassDesc getDtoClassDesc() {

        return creator_.newClassDesc(dtoClassName_);
    }

    public ClassDesc getBeanClassDesc() {

        return creator_.newClassDesc(beanClassName_);
    }

    public ClassDesc getDaoClassDesc() {

        return creator_.newClassDesc(daoClassName_);
    }

    public ClassDesc getDxoClassDesc() {

        return creator_.newClassDesc(dxoClassName_);
    }
}
