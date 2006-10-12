package org.seasar.cms.beantable.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.beantable.Beantable;
import org.seasar.cms.database.identity.ColumnMetaData;

public class BeantableListHandler<T> extends BeantableHandler<T> {

    public BeantableListHandler(Beantable<T> beantable) {
        super(beantable);
    }

    public Object handle(ResultSet rs) throws SQLException {

        List<T> list = new ArrayList<T>();

        if (!rs.next()) {
            return list;
        }

        ColumnMetaData[] columns = getColumns(rs.getMetaData());
        do {
            list.add(handle(rs, columns));
        } while (rs.next());
        return list;
    }
}
