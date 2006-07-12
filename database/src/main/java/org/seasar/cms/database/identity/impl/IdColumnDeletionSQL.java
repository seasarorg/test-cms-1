package org.seasar.cms.database.identity.impl;

public class IdColumnDeletionSQL {

    private String[] deletionSQLs_;

    public IdColumnDeletionSQL(String[] deletionSQLs) {
        deletionSQLs_ = deletionSQLs;
    }

    public String[] getDeletionSQLs() {
        return deletionSQLs_;
    }
}
