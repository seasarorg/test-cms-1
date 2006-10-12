package org.seasar.cms.beantable.impl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;
import org.seasar.cms.beantable.Beantable;
import org.seasar.cms.database.identity.ColumnMetaData;

public class BeantableHandler<T> implements ResultSetHandler {

    private Beantable<T> beantable_;

    public BeantableHandler(Beantable<T> beantable) {
        beantable_ = beantable;
    }

    public Object handle(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return handle(rs, getColumns(rs.getMetaData()));
        } else {
            return null;
        }
    }

    protected T handle(ResultSet rs, ColumnMetaData[] columns)
            throws SQLException {
        T bean = beantable_.newBeanInstance();
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] != null) {
                beantable_.setValue(bean, columns[i], rs.getObject(i + 1));
            }
        }
        return bean;
    }

    protected ColumnMetaData[] getColumns(ResultSetMetaData rsmd)
            throws SQLException {
        int cols = rsmd.getColumnCount();
        ColumnMetaData[] columns = new ColumnMetaData[cols];
        for (int i = 0; i < cols; i++) {
            columns[i] = beantable_.getTableMetaData().getColumn(
                    rsmd.getColumnName(i + 1));
        }
        return columns;
    }
}
