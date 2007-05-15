package org.seasar.cms.database.sql;

import java.sql.SQLException;

import javax.sql.XAConnection;

import org.seasar.extension.dbcp.impl.XAConnectionImpl;
import org.seasar.extension.dbcp.impl.XADataSourceImpl;

public class VoidDataSource extends XADataSourceImpl {
    @Override
    public XAConnection getXAConnection() throws SQLException {
        return new XAConnectionImpl(new VoidConnection());
    }
}
