package org.seasar.cms.database.identity;

public class TableMetaData {

    private String name_;

    private ColumnMetaData[] columns_;

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
        return columns_;
    }

    public void setColumns(ColumnMetaData[] columns) {
        columns_ = columns;
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
