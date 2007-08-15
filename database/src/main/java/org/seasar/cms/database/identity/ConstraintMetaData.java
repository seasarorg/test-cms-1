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

    public static final String FOREIGN_KEY = "FOREIGN KEY";

    public static final String CHECK = "CHECK";

    private String name_;

    private String[] columnNames_;

    private String rawBody_;

    public String getName() {
        return name_;
    }

    public void setName(String name) {
        name_ = name;
    }

    public String[] getColumnNames() {
        return columnNames_;
    }

    public void setColumnNames(String[] columnNames) {
        columnNames_ = columnNames;
    }

    public String getRawBody() {
        return rawBody_;
    }

    public void setRawBody(String rawBody) {
        rawBody_ = rawBody;
    }
}
