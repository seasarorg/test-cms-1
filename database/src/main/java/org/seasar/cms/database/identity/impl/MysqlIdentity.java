package org.seasar.cms.database.identity.impl;

/**
 * <p>
 * <b>同期化：</b> このクラスはスレッドセーフです。
 * </p>
 *
 * @author YOKOTA Takehiko
 */
public class MysqlIdentity extends AbstractIdentity {

    public String getDatabaseProductId() {

        return "mysql";
    }

    public boolean isMatched(String productName, String productVersion) {
        // XXX 実装しよう。
        return false;
    }

    @Override
    public SQLToDefineIdColumn getSQLToDefineIdColumn(String tableName,
        String columnName, String sequenceName) {
        if (sequenceName == null) {
            return new SQLToDefineIdColumn("INTEGER NOT NULL AUTO INCREMENT",
                new String[0]);
        } else {
            return super.getSQLToDefineIdColumn(tableName, columnName,
                sequenceName);
        }
    }

    @Override
    public SQLToDeleteIdColumn getSQLToDeleteIdColumn(String tableName,
        String columnName, String sequenceName) {
        if (sequenceName == null) {
            return new SQLToDeleteIdColumn(new String[0]);
        } else {
            return super.getSQLToDeleteIdColumn(tableName, columnName,
                sequenceName);
        }
    }

    @Override
    public String getSQLToGetGeneratedId(String tableName, String columnName,
        String sequenceName) {
        if (sequenceName == null) {
            return "SELECT LAST_INSERT_ID()";
        } else {
            return super.getSQLToGetGeneratedId(tableName, columnName,
                sequenceName);
        }
    }
}
