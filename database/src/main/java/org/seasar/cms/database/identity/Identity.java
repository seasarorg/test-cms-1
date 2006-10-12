package org.seasar.cms.database.identity;

import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * <p>
 * <b>同期化：</b> このインタフェースの実装クラスはスレッドセーフである必要があります。
 * </p>
 *
 * @author YOKOTA Takehiko
 */
public interface Identity {
    String getDatabaseProductId();

    boolean startUsingDatabase();

    void stopUsingDatabase();

    boolean isMatched(String productName, String productVersion);

    /**
     * 指定されたテーブルが存在するかどうかを返します。
     *
     * @param tableName
     *            テーブル名。
     * @return テーブルが存在するかどうか。
     */
    boolean existsTable(String tableName) throws SQLException;

    /**
     * 指定された式を数値に変換するような式を返します。
     *
     * @param expr 式。
     * @return 数値として扱える式。
     */
    String toNumericExpression(String expr);

    boolean createTable(TableMetaData table) throws SQLException;

    boolean createTable(TableMetaData table, boolean force) throws SQLException;

    boolean correctTableSchema(TableMetaData table) throws SQLException;

    boolean correctTableSchema(TableMetaData table, boolean force)
        throws SQLException;

    boolean dropTable(TableMetaData table) throws SQLException;

    boolean dropTable(TableMetaData table, boolean force) throws SQLException;

    /**
     * 指定されたテーブルが持つ全てのカラム名を返します。
     *
     * @param tableName テーブル名。
     * @return カラム名の配列。nullを返すことはありません。
     */
    String[] getColumns(String tableName) throws SQLException;

    Integer getGeneratedId(TableMetaData table) throws SQLException;

    void setDataSource(DataSource ds);

    String getSQLToGenerateNextId(TableMetaData table, ColumnMetaData column);
}