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

    public IdColumnDefinitionSQL getIdColumnDefinitionSQL(String tableName,
        String columnName, String sequenceName) {
        if (sequenceName == null) {
            return new IdColumnDefinitionSQL("INTEGER NOT NULL AUTO INCREMENT",
                new String[0]);
        } else {
            return new IdColumnDefinitionSQL("INTEGER DEFAULT NEXTVAL('"
                + sequenceName + "') NOT NULL PRIMARY KEY",
                new String[] { "CREATE SEQUENCE " + sequenceName });
        }
    }
}
