package org.seasar.cms.beantable;

import java.sql.SQLException;

public interface BeantableDao {

    Object execute(String queryName, Object[] params, Class<?>[] paramTypes,
            Class<?> returnType) throws SQLException;
}
