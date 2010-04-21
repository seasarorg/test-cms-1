package org.seasar.cms.database.identity.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbutils.DbUtils;

/**
 * <p>
 * <b>同期化：</b> このクラスはスレッドセーフです。
 * </p>
 *
 * @author YOKOTA Takehiko
 */
public class H2Identity extends AbstractIdentity {

    public String getDatabaseProductId() {
        return "h2";
    }

    public boolean isMatched(String productName, String productVersion) {
        if ("H2".equals(productName)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean existsTable(String tableName) throws SQLException {
        return super.existsTable(tableName.toUpperCase());
    }

    @Override
    public String[] getColumns(String tableName) throws SQLException {
        return super.getColumns(tableName.toUpperCase());
    }

    @Override
    public SQLToDefineIdColumn getSQLToDefineIdColumn(String tableName,
            String columnName, String sequenceName) {
        if (sequenceName == null) {
            return new SQLToDefineIdColumn("INTEGER NOT NULL IDENTITY",
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
    public void stopUsingDatabase() {
        Connection con = null;
        Statement st = null;
        try {
            con = ds_.getConnection();
            st = con.createStatement();
            st.executeUpdate("SHUTDOWN COMPACT");
        } catch (Throwable t) {
            // H2はデフォルトでシャットダウンフックにDatabaseのクローズ処理を登録しているため、
            // ここでは処理が続行できないことがある。その場合は無視する。
        } finally {
            try {
                DbUtils.closeQuietly(con, st, null);
            } catch (Throwable ignore) {
                // S2のConnectionPoolImplはSQLExceptionをSQLRuntimeExceptionにラップしてスローすることがあるため
                // こうしている。
            }
        }
    }

    @Override
    public String getSQLToGetGeneratedId(String tableName, String columnName,
            String sequenceName) {
        if (sequenceName == null) {
            return "CALL IDENTITY()";
        } else {
            return super.getSQLToGetGeneratedId(tableName, columnName,
                    sequenceName);
        }
    }
}
