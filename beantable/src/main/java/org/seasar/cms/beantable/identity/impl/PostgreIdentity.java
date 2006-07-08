package org.seasar.cms.beantable.identity.impl;

import org.seasar.cms.beantable.identity.IdColumnDefinitionSQL;
import org.seasar.cms.beantable.identity.IdColumnDeletionSQL;

/**
 * <p>
 * <b>同期化：</b> このクラスはスレッドセーフです。
 * </p>
 * 
 * @author YOKOTA Takehiko
 */
public class PostgreIdentity extends AbstractIdentity {

    public String getDatabaseProductId() {

        return "postgre";
    }

    public boolean isMatched(String productName, String productVersion) {
        // XXX 実装しよう。
        return false;
    }

    public String toNumericExpression(String expr) {
        return "(to_number(" + expr + ",'9999999999')";
    }

    public IdColumnDefinitionSQL getIdColumnDefinitionSQL(String tableName,
        String columnName, String sequenceName) {
        if (sequenceName == null) {
            sequenceName = getSequenceName(tableName, columnName);
        }
        return new IdColumnDefinitionSQL("INTEGER DEFAULT NEXTVAL('"
            + sequenceName + "') NOT NULL PRIMARY KEY",
            new String[] { "CREATE SEQUENCE " + sequenceName });
    }

    public IdColumnDeletionSQL getIdColumnDeletionSQL(
        String tableName, String columnName, String sequenceName) {
        return new IdColumnDeletionSQL(new String[] { "DROP SEQUENCE "
            + getSequenceName(tableName, columnName) });
    }
}
