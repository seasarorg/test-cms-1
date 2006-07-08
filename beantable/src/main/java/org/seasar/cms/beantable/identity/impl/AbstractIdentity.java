package org.seasar.cms.beantable.identity.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.seasar.cms.beantable.identity.IdColumnDeletionSQL;
import org.seasar.cms.beantable.identity.Identity;
import org.seasar.cms.beantable.impl.ColumnMetaData;
import org.seasar.framework.container.annotation.tiger.Aspect;
import org.seasar.framework.log.Logger;

/**
 * <p>
 * <b>同期化：</b> このクラスはスレッドセーフです。
 * </p>
 * 
 * @author YOKOTA Takehiko
 */
abstract public class AbstractIdentity implements Identity {
    protected DataSource ds_;

    protected Logger log_ = Logger.getLogger(getClass());

    /*
     * Identity
     */

    public boolean startUsingDatabase() {
        return true;
    }

    public void stopUsingDatabase() {
    }

    @Aspect("j2ee.requiredTx")
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

    public String getAlterTableAddColumnSQL(String tableName,
        ColumnMetaData column) {
        return "ALTER TABLE " + tableName + " ADD COLUMN " + column.getName()
            + " " + column.getDefinitionSQL(tableName);
    }

    public String getAlterTableDropColumnSQL(String tableName, String columnName) {
        return "ALTER TABLE " + tableName + " DROP COLUMN " + columnName;
    }

    @Aspect("j2ee.requiredTx")
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

    public IdColumnDeletionSQL getIdColumnDeletionSQL(String tableName,
        String columnName, String sequenceName) {
        if (sequenceName == null) {
            return new IdColumnDeletionSQL(new String[0]);
        } else {
            return new IdColumnDeletionSQL(new String[] { "DROP SEQUENCE "
                + getSequenceName(tableName, columnName) });
        }
    }

    protected String getSequenceName(String tableName, String columnName) {
        return "_SEQ_" + tableName + "_" + columnName;
    }

    /*
     * for framework
     */

    public void setDataSource(DataSource ds) {
        ds_ = ds;
    }
}
