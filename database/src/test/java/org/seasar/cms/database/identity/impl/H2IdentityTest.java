package org.seasar.cms.database.identity.impl;

import org.seasar.cms.database.identity.ColumnMetaData;
import org.seasar.cms.database.identity.ConstraintMetaData;
import org.seasar.cms.database.identity.IndexMetaData;
import org.seasar.cms.database.identity.ReferencesMetaData;
import org.seasar.cms.database.identity.TableMetaData;

import junit.framework.TestCase;

public class H2IdentityTest extends TestCase {
    private H2Identity target_ = new H2Identity();

    public void testConstructCreateTableSQLs() throws Exception {
        TableMetaData table = new TableMetaData();
        table.setName("hoge");
        ColumnMetaData column = new ColumnMetaData();
        column.setName("a");
        column.setJdbcTypeName("INTEGER");
        table.setColumns(new ColumnMetaData[] { column });
        ConstraintMetaData constraint1 = new ConstraintMetaData();
        constraint1.setName("PRIMARY KEY");
        constraint1.setColumnNames(new String[] { "b", "c" });
        ConstraintMetaData constraint2 = new ConstraintMetaData();
        constraint2.setRawBody("FOREIGN KEY (b) REFERENCES fuga (b)");
        table.setConstraints(new ConstraintMetaData[] { constraint1,
            constraint2 });
        IndexMetaData index = new IndexMetaData();
        index.setName("INDEX1");
        index.setColumnNames(new String[] { "a", "b" });
        table.setIndexes(new IndexMetaData[] { index });

        String[] actual = target_.constructCreateTableSQLs(table);

        assertEquals(1, actual.length);
        assertEquals(
            "CREATE TABLE hoge (a INTEGER, PRIMARY KEY (b, c), FOREIGN KEY (b) REFERENCES fuga (b))",
            actual[0]);
    }

    public void testGetColumnDefinitionSQL() throws Exception {
        ColumnMetaData column = new ColumnMetaData();
        column.setName("a");
        column.setJdbcTypeName("INTEGER");
        column.setCheck("a > 0");
        column.setDefault("1");
        column.setDetail("DETAIL");
        column.setNotNull(true);
        ReferencesMetaData references = new ReferencesMetaData();
        references.setColumnName("a");
        references.setTableName("fuga");
        references.setSpec("SPEC");
        column.setReferences(references);

        String actual = target_.constructColumnDefinitionSQL("hoge", column);

        assertEquals(
            "INTEGER DEFAULT 1 NOT NULL CHECK(a > 0) REFERENCES fuga (a) SPEC DETAIL",
            actual);
    }
}
