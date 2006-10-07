package org.seasar.cms.database.identity;

import java.util.LinkedHashMap;
import java.util.Map;

public class TableMetaData {

    private String name_;

    private Map<String, ColumnMetaData> columnMap_ = new LinkedHashMap<String, ColumnMetaData>();

    private ConstraintMetaData[] constraints_;

    private IndexMetaData[] indexes_;

    public TableMetaData() {
        this(null);
    }

    public TableMetaData(String name) {
        this(name, new ColumnMetaData[0], new ConstraintMetaData[0],
            new IndexMetaData[0]);
    }

    public TableMetaData(String name, ColumnMetaData[] columns,
        ConstraintMetaData[] constraints, IndexMetaData[] indexes) {
        setName(name);
        setColumns(columns);
        setConstraints(constraints);
    }

    public ColumnMetaData[] getColumns() {
        return columnMap_.values().toArray(new ColumnMetaData[0]);
    }

    public void setColumns(ColumnMetaData[] columns) {
        columnMap_.clear();
        for (int i = 0; i < columns.length; i++) {
            columnMap_.put(columns[i].getName(), columns[i]);
        }
    }

    public ColumnMetaData getColumn(String columnName) {
        return columnMap_.get(columnName);
    }

    public ConstraintMetaData[] getConstraints() {
        return constraints_;
    }

    public void setConstraints(ConstraintMetaData[] constraints) {
        constraints_ = constraints;
    }

    public IndexMetaData[] getIndexes() {
        return indexes_;
    }

    public void setIndexes(IndexMetaData[] indexes) {
        indexes_ = indexes;
    }

    public String getName() {
        return name_;
    }

    public void setName(String name) {
        name_ = name;
    }
}
