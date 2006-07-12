package org.seasar.cms.database.identity.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbutils.DbUtils;
import org.hsqldb.DatabaseManager;
import org.hsqldb.lib.HsqlTimer;

/**
 * <p>
 * <b>同期化：</b> このクラスはスレッドセーフです。
 * </p>
 * 
 * @author YOKOTA Takehiko
 */
public class HsqlIdentity extends AbstractIdentity {

    public String getDatabaseProductId() {
        return "hsql";
    }

    public boolean isMatched(String productName, String productVersion) {
        if ("HSQL Database Engine".equals(productName)) {
            return true;
        }

        return false;
    }

    public boolean existsTable(String tableName) throws SQLException {
        return super.existsTable(tableName.toUpperCase());
    }

    public String[] getColumns(String tableName) throws SQLException {
        return super.getColumns(tableName.toUpperCase());
    }

    public IdColumnDefinitionSQL getIdColumnDefinitionSQL(String tableName,
        String columnName, String sequenceName) {
        if (sequenceName == null) {
            return new IdColumnDefinitionSQL("INTEGER NOT NULL IDENTITY",
                new String[0]);
        } else {
            return new IdColumnDefinitionSQL("INTEGER NOT NULL PRIMARY KEY",
                new String[] { "CREATE SEQUENCE " + sequenceName });
        }
    }

    @Override
    public boolean startUsingDatabase() {
        Connection con = null;
        Statement st = null;
        try {
            con = ds_.getConnection();
            st = con.createStatement();
            st
                .executeUpdate("SET PROPERTY \"hsqldb.default_table_type\" 'cached'");
        } catch (SQLException ex) {
            throw new RuntimeException("Can't start identity", ex);
        } finally {
            DbUtils.closeQuietly(con, st, null);
        }

        return true;
    }

    @Override
    public void stopUsingDatabase() {
        Connection con = null;
        Statement st = null;
        try {
            con = ds_.getConnection();
            st = con.createStatement();
            st.executeUpdate("SHUTDOWN COMPACT");
        } catch (SQLException ex) {
            throw new RuntimeException("Can't shutdown", ex);
        } finally {
            DbUtils.closeQuietly(con, st, null);
        }

        // Timer Threadを停止する。
        HsqlTimer timer = DatabaseManager.getTimer();
        if (timer != null) {
            timer.shutDown();
        }
    }
}
