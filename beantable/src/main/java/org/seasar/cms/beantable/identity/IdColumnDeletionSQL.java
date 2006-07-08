package org.seasar.cms.beantable.identity;

public class IdColumnDeletionSQL {

    private String[] deletionSQLs_;

    public IdColumnDeletionSQL(String[] deletionSQLs) {
        deletionSQLs_ = deletionSQLs;
    }

    public String[] getDeletionSQLs() {
        return deletionSQLs_;
    }
}
