package org.seasar.cms.beantable;

import org.apache.commons.dbutils.ResultSetHandler;

public interface BeantableDao<T> {

    Beantable<T> getBeantable();

    String getQuery(String name);

    ResultSetHandler getBeantableHandler();

    ResultSetHandler getBeantableListHandler();
}
