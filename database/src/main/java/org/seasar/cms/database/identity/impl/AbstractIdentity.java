package org.seasar.cms.database.identity.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.seasar.cms.database.identity.ColumnMetaData;
import org.seasar.cms.database.identity.ConstraintMetaData;
import org.seasar.cms.database.identity.Identity;
import org.seasar.cms.database.identity.IndexMetaData;
import org.seasar.cms.database.identity.TableMetaData;

/**
 * <p>
 * <b>同期化：</b> このクラスはスレッドセーフです。
 * </p>
 *
 * @author YOKOTA Takehiko
 */
abstract public class AbstractIdentity implements Identity {

    protected DataSource ds_;

    /*
     * Identity
     */

    /**
     * 自動採番されるカラムを定義するための部分SQLを返します。
     * <p>シーケンス名を明示的に指定した場合はシーケンスを利用して自動採番するような
     * 部分SQLを返します。
     * シーケンス名としてnullを指定した場合はDBMSの実装に依存します。
     * </p>
     *
     * @param tableName テーブル名。
     * @param columnName カラム名。
     * @param sequenceName シーケンス名。nullを指定することもできます。
     * @return 自動採番されるカラムを定義するための部分SQL。
     */
    public SQLToDefineIdColumn getSQLToDefineIdColumn(String tableName,
        String columnName, String sequenceName) {
        if (sequenceName == null) {
            sequenceName = getSequenceName(tableName, columnName);
        }
        return new SQLToDefineIdColumn("INTEGER DEFAULT NEXTVAL('"
            + sequenceName + "') NOT NULL PRIMARY KEY",
            new String[] { "CREATE SEQUENCE " + sequenceName });
    }

    public boolean startUsingDatabase() {
        return true;
    }

    public void stopUsingDatabase() {
    }

    public boolean existsTable(String tableName) throws SQLException {
        Connection con = null;
        ResultSet rs = null;
        try {
            con = ds_.getConnection();

            DatabaseMetaData dmd = con.getMetaData();
            rs = dmd.getTables(null, null, tableName, null);
            return rs.next();
        } finally {
            DbUtils.closeQuietly(con, null, rs);
        }
    }

    public String toNumericExpression(String expr) {
        return "(" + expr + "+0)";
    }

    public boolean createTable(TableMetaData table) throws SQLException {
        return createTable(table, false);
    }

    public boolean createTable(TableMetaData table, boolean force)
        throws SQLException {

        if (!force && existsTable(table.getName())) {
            return false;
        }

        executeUpdate(constructCreateTableSQLs(table), force);
        executeUpdate(constructCreateIndexSQLs(table), force);

        return true;
    }

    public boolean correctTableSchema(TableMetaData table) throws SQLException {

        return correctTableSchema(table, false);
    }

    public boolean correctTableSchema(TableMetaData table, boolean force)
        throws SQLException {

        boolean modified = false;

        String tableName = table.getName();
        ColumnMetaData[] columns = table.getColumns();

        Set<String> currentColumnSet = new HashSet<String>();
        String[] columnNames = getColumns(tableName);
        for (int i = 0; i < columnNames.length; i++) {
            currentColumnSet.add(columnNames[i]);
        }
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i].getName();
            if (currentColumnSet.contains(columnName)) {
                currentColumnSet.remove(columnName);
            } else {
                addColumn(tableName, columns[i], force);
                modified = true;
            }
        }
        for (Iterator itr = currentColumnSet.iterator(); itr.hasNext();) {
            dropColumn(tableName, (String) itr.next(), force);
            modified = true;
        }

        return modified;
    }

    public boolean dropTable(TableMetaData table) throws SQLException {
        return dropTable(table, false);
    }

    public boolean dropTable(TableMetaData table, boolean force)
        throws SQLException {

        if (!force && !existsTable(table.getName())) {
            return false;
        }

        executeUpdate(constructDropTableSQLs(table), force);
        executeUpdate(constructDropIndexSQLs(table), force);

        return true;
    }

    void addColumn(String tableName, ColumnMetaData column, boolean force)
        throws SQLException {
        executeUpdate(getSQLToAlterTableAddColumn(tableName, column), force);
    }

    void dropColumn(String tableName, String columnName, boolean force)
        throws SQLException {
        executeUpdate(getSQLToAlterTableDropColumn(tableName, columnName),
            force);
    }

    protected String[] constructCreateTableSQLs(TableMetaData table) {
        List<String> sqlList = new ArrayList<String>();

        String tableName = table.getName();
        ColumnMetaData[] columns = table.getColumns();
        ConstraintMetaData[] constraints = table.getConstraints();
        String[] details = table.getDetails();

        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE ").append(tableName).append(" (");
        String delim = "";
        for (int i = 0; i < columns.length; i++) {
            sb.append(delim);
            delim = ", ";
            String columnName = columns[i].getName();
            sb.append(columnName).append(" ").append(
                getDefinitionSQL(tableName, columns[i]));
            sqlList.addAll(Arrays.asList(getAdditionalDefinitionSQLs(tableName,
                columns[i])));
        }
        for (int i = 0; i < constraints.length; i++) {
            String[] names = constraints[i].getColumnNames();
            if (names.length == 0) {
                continue;
            }
            sb.append(delim);
            delim = ", ";
            sb.append(constraints[i].getName()).append(" (");
            String delim2 = "";
            for (int j = 0; j < names.length; j++) {
                sb.append(delim2);
                delim2 = ", ";
                sb.append(names[j]);
            }
            sb.append(")");
        }
        for (int i = 0; i < details.length; i++) {
            sb.append(delim);
            delim = ", ";
            sb.append(details[i]);
        }
        sb.append(")");
        sqlList.add(sb.toString());

        return sqlList.toArray(new String[0]);
    }

    protected String[] constructCreateIndexSQLs(TableMetaData table) {
        List<String> indexList = new ArrayList<String>();

        String tableName = table.getName();
        ColumnMetaData[] columns = table.getColumns();
        IndexMetaData[] indexes = table.getIndexes();

        for (int i = 0; i < columns.length; i++) {
            if (!columns[i].isIndexCreated()) {
                continue;
            }
            indexList.add("CREATE INDEX "
                + getIndexName(tableName, columns[i].getName()) + " ("
                + columns[i].getName() + ")");
        }

        for (int i = 0; i < indexes.length; i++) {
            String[] columnNames = indexes[i].getColumnNames();
            if (columnNames.length == 0) {
                continue;
            }
            String indexName = indexes[i].getName();
            StringBuffer sb = new StringBuffer();
            sb.append("CREATE INDEX ");
            if (indexName == null) {
                appendIndexName(sb, tableName, columnNames);
            } else {
                sb.append(indexName);
            }
            sb.append(" (");
            String delim = "";
            for (int j = 0; j < columnNames.length; j++) {
                sb.append(delim);
                delim = ", ";
                sb.append(columnNames[j]);
            }
            sb.append(")");
            indexList.add(sb.toString());
        }

        return indexList.toArray(new String[0]);
    }

    StringBuffer appendIndexName(StringBuffer sb, String tableName,
        String[] columnNames) {
        sb.append("_IDX_").append(tableName);
        for (int i = 0; i < columnNames.length; i++) {
            sb.append("_").append(columnNames[i]);
        }
        return sb;
    }

    protected String[] constructDropTableSQLs(TableMetaData table) {
        List<String> sqlList = new ArrayList<String>();

        String tableName = table.getName();
        ColumnMetaData[] columns = table.getColumns();

        sqlList.add("DROP TABLE " + tableName);
        for (int i = 0; i < columns.length; i++) {
            sqlList.addAll(Arrays
                .asList(getDeletionSQLs(tableName, columns[i])));
        }

        return sqlList.toArray(new String[0]);
    }

    protected String[] constructDropIndexSQLs(TableMetaData table) {
        List<String> indexList = new ArrayList<String>();

        String tableName = table.getName();
        ColumnMetaData[] columns = table.getColumns();
        IndexMetaData[] indexes = table.getIndexes();

        for (int i = 0; i < columns.length; i++) {
            if (!columns[i].isIndexCreated()) {
                continue;
            }
            indexList.add("DROP INDEX "
                + getIndexName(tableName, columns[i].getName()));
        }

        for (int i = 0; i < indexes.length; i++) {
            String[] columnNames = indexes[i].getColumnNames();
            if (columnNames.length == 0) {
                continue;
            }
            String indexName = indexes[i].getName();
            StringBuffer sb = new StringBuffer();
            sb.append("DROP INDEX ");
            if (indexName == null) {
                appendIndexName(sb, tableName, columnNames);
            } else {
                sb.append(indexName);
            }
            indexList.add(sb.toString());
        }

        return indexList.toArray(new String[0]);
    }

    /**
     * 指定されたテーブルに指定されたカラムを追加するようなSQLを返します。
     *
     * @param tableName テーブル名。
     * @param column 追加するカラムの情報。
     * @return カラムを追加するためのSQL。
     */
    protected String getSQLToAlterTableAddColumn(String tableName,
        ColumnMetaData column) {
        return "ALTER TABLE " + tableName + " ADD COLUMN " + column.getName()
            + " " + getDefinitionSQL(tableName, column);
    }

    /**
     * 指定されたテーブルから指定されたカラムを削除するようなSQLを返します。
     *
     * @param tableName テーブル名。
     * @param columnName 削除するカラム名。
     * @return カラムを削除するためのSQL。
     */
    protected String getSQLToAlterTableDropColumn(String tableName,
        String columnName) {
        return "ALTER TABLE " + tableName + " DROP COLUMN " + columnName;
    }

    public String[] getColumns(String tableName) throws SQLException {
        Connection con = null;
        ResultSet rs = null;
        try {
            con = ds_.getConnection();

            DatabaseMetaData dmd = con.getMetaData();
            rs = dmd.getColumns(null, null, tableName, "%");
            List<String> list = new ArrayList<String>();
            while (rs.next()) {
                list.add(rs.getString("COLUMN_NAME"));
            }
            return list.toArray(new String[0]);
        } finally {
            DbUtils.closeQuietly(con, null, rs);
        }
    }

    /**
     * 自動採番されるカラムを削除するための部分SQLを返します。
     * <p>シーケンス名を明示的に指定した場合はシーケンスを利用して自動採番するような
     * カラムとみなし、対応するシーケンスも削除するような部分SQLを返します。
     * </p>
     *
     * @param tableName テーブル名。
     * @param columnName カラム名。
     * @param sequenceName シーケンス名。nullを指定することもできます。
     * @return 自動採番されるカラムを削除するための部分SQL。
     */
    public SQLToDeleteIdColumn getSQLToDeleteIdColumn(String tableName,
        String columnName, String sequenceName) {
        if (sequenceName == null) {
            sequenceName = getSequenceName(tableName, columnName);
        }
        return new SQLToDeleteIdColumn(new String[] { "DROP SEQUENCE "
            + sequenceName });
    }

    protected String getSequenceName(String tableName, String columnName) {
        return "_SEQ_" + tableName + "_" + columnName;
    }

    protected String getDefinitionSQL(String tableName, ColumnMetaData column) {

        if (column.isId()) {
            return getSQLToDefineIdColumn(tableName, column.getName(),
                column.getSequenceName()).getColumnDefinitionSQL();
        }

        StringBuffer sb = new StringBuffer();
        sb.append(column.getJdbcTypeName());
        String defaults = column.getDefault();
        if (defaults != null && defaults.length() > 0) {
            sb.append(" DEFAULT ").append(defaults);
        }
        if (column.isNotNull()) {
            sb.append(" NOT NULL");
        }
        if (column.isPrimaryKey()) {
            sb.append(" PRIMARY KEY");
        } else if (column.isUnique()) {
            sb.append(" UNIQUE");
        }
        String detail = column.getDetail();
        if (detail != null && detail.length() > 0) {
            sb.append(" ").append(detail);
        }

        return sb.toString();
    }

    protected String[] getAdditionalDefinitionSQLs(String tableName,
        ColumnMetaData column) {
        if (column.isId()) {
            return getSQLToDefineIdColumn(tableName, column.getName(),
                column.getSequenceName()).getAdditionalCreationSQLs();
        } else {
            return new String[0];
        }
    }

    protected String[] getDeletionSQLs(String tableName, ColumnMetaData column) {
        if (column.isId()) {
            return getSQLToDeleteIdColumn(tableName, column.getName(),
                column.getSequenceName()).getDeletionSQLs();
        } else {
            return new String[0];
        }
    }

    protected String getIndexName(String tableName, String columnName) {
        return "_IDX_" + tableName + "_" + columnName;
    }

    protected void executeUpdate(String sql, boolean force) throws SQLException {
        executeUpdate(new String[] { sql }, force);
    }

    protected void executeUpdate(String[] sqls, boolean force)
        throws SQLException {

        Connection con = null;
        try {
            con = ds_.getConnection();
            for (int i = 0; i < sqls.length; i++) {
                Statement st = null;
                try {
                    st = con.createStatement();
                    System.out.println("EXECUTE: " + sqls[i]);
                    st.executeUpdate(sqls[i]);
                } catch (SQLException ex) {
                    if (!force) {
                        throw ex;
                    }
                } finally {
                    DbUtils.closeQuietly(st);
                }
            }
        } finally {
            DbUtils.closeQuietly(con);
        }
    }

    public Integer getGeneratedId(TableMetaData table) throws SQLException {

        ColumnMetaData idColumn = table.getIdColumn();
        if (idColumn == null) {
            return null;
        }

        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            con = ds_.getConnection();
            st = con.createStatement();
            rs = st.executeQuery(getSQLToGetGeneratedId(table.getName(),
                idColumn.getName(), idColumn.getSequenceName()));
            rs.next();
            return new Integer(rs.getInt(1));
        } finally {
            DbUtils.closeQuietly(con, st, rs);
        }
    }

    public String getSQLToGetGeneratedId(String tableName, String columnName,
        String sequenceName) {
        if (sequenceName == null) {
            sequenceName = getSequenceName(tableName, columnName);
        }
        return "SELECT CURRVAL('" + sequenceName + "')";
    }

    public String getSQLToGenerateNextId(TableMetaData table,
        ColumnMetaData column) {
        return null;
    }

    /*
     * for framework
     */

    public void setDataSource(DataSource ds) {
        ds_ = ds;
    }
}
