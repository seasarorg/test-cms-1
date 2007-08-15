package org.seasar.cms.database.identity;

/**
 * <p><b>同期化：</b>
 * このクラスはスレッドセーフではありません。
 * </p>
 * 
 * @author YOKOTA Takehiko
 */
public class ReferencesMetaData {
    private String tableName_;

    private String columnName_;

    private String spec_;

    public String getColumnName() {
        return columnName_;
    }

    public void setColumnName(String columnName) {
        columnName_ = columnName;
    }

    public String getSpec() {
        return spec_;
    }

    public void setSpec(String spec) {
        spec_ = spec;
    }

    public String getTableName() {
        return tableName_;
    }

    public void setTableName(String tableName) {
        tableName_ = tableName;
    }
}
