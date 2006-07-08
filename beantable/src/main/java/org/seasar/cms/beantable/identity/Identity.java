package org.seasar.cms.beantable.identity;

import java.sql.SQLException;

import org.seasar.cms.beantable.impl.ColumnMetaData;

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

    /**
     * 指定されたテーブルに指定されたカラムを追加するようなSQLを返します。
     * 
     * @param tableName テーブル名。
     * @param column 追加するカラムの情報。
     * @return カラムを追加するためのSQL。
     */
    String getAlterTableAddColumnSQL(String tableName, ColumnMetaData column);

    /**
     * 指定されたテーブルから指定されたカラムを削除するようなSQLを返します。
     * 
     * @param tableName テーブル名。
     * @param columnName 削除するカラム名。
     * @return カラムを削除するためのSQL。
     */
    String getAlterTableDropColumnSQL(String tableName, String columnName);

    /**
     * 指定されたテーブルが持つ全てのカラム名を返します。
     * 
     * @param tableName テーブル名。
     * @return カラム名の配列。nullを返すことはありません。
     */
    String[] getColumns(String tableName) throws SQLException;

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
    IdColumnDefinitionSQL getIdColumnDefinitionSQL(String tableName,
        String columnName, String sequenceName);

    /**
     * 自動採番されるカラムを削除するための部分SQLを返します。
     * <p>シーケンス名を明示的に指定した場合はシーケンスを利用して自動採番するような
     * カラムとみなし、対応するシーケンスも削除するような部分SQLを返します。
     * </p>
     * 
     * @param tableName テーブル名。
     * @param columnName カラム名。
     * @param sequenceName TODO
     * @param sequenceName シーケンス名。nullを指定することもできます。
     * @return 自動採番されるカラムを削除するための部分SQL。
     */
    IdColumnDeletionSQL getIdColumnDeletionSQL(String tableName,
        String columnName, String sequenceName);
}