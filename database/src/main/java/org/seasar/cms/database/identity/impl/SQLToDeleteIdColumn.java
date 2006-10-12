package org.seasar.cms.database.identity.impl;

public class SQLToDeleteIdColumn {

    private String[] deletionSQLs_;

    public SQLToDeleteIdColumn(String[] deletionSQLs) {
        deletionSQLs_ = deletionSQLs;
    }

    public String[] getDeletionSQLs() {
        return deletionSQLs_;
    }
}
