package org.seasar.cms.database.identity;

public class IndexMetaData {

    private String name_;

    private String[] columnNames_;

    public IndexMetaData() {
    }

    public IndexMetaData(String[] columnNames) {
        this(columnNames, null);
    }

    public IndexMetaData(String[] columnNames, String name) {
        setColumnNames(columnNames);
    }

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
