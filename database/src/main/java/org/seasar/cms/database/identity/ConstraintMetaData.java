package org.seasar.cms.database.identity;

/**
 * <p><b>同期化：</b>
 * このクラスはスレッドセーフではありません。
 * </p>
 * 
 * @author YOKOTA Takehiko
 */
public class ConstraintMetaData {
    public static final String PRIMARY_KEY = "PRIMARY KEY";

    public static final String UNIQUE = "UNIQUE";

    private String[] columnNames_;

    private String name_;

    public String[] getColumnNames() {
        return columnNames_;
    }

    public void setColumnNames(String[] columnNames) {
        columnNames_ = columnNames;
    }

    public String getName() {
        return name_;
    }

    public void setName(String name) {
        name_ = name;
    }
}
