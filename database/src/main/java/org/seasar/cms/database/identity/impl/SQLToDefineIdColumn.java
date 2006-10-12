package org.seasar.cms.database.identity.impl;

/**
 * <p><b>同期化：</b>
 * このクラスはスレッドセーフです。</p>
 * 
 * @author YOKOTA Takehiko
 */
public class SQLToDefineIdColumn {
    private String columnDefinitionSQL_;

    private String[] additionalCreationSQLs_;

    public SQLToDefineIdColumn(String columnDefinitionSQL,
        String[] additionalCreationSQLs) {
        columnDefinitionSQL_ = columnDefinitionSQL;
        additionalCreationSQLs_ = additionalCreationSQLs;
    }

    public String[] getAdditionalCreationSQLs() {
        return additionalCreationSQLs_;
    }

    public String getColumnDefinitionSQL() {
        return columnDefinitionSQL_;
    }
}
