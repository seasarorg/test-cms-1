package org.seasar.cms.beantable.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;

/**
 * 
 * <p><b>同期化：</b>
 * このクラスはスレッドセーフです。</p>
 * 
 * @author YOKOTA Takehiko
 */
public class ScalarListHandler implements ResultSetHandler {

    public Object handle(ResultSet rs) throws SQLException {
        List<Object> list = new ArrayList<Object>();
        while (rs.next()) {
            list.add(rs.getObject(1));
        }
        return list;
    }
}
