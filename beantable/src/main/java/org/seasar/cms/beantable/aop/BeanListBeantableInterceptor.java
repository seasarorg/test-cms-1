package org.seasar.cms.beantable.aop;

import org.apache.commons.dbutils.ResultSetHandler;
import org.seasar.cms.beantable.BeantableDao;

public class BeanListBeantableInterceptor extends BeantableInterceptor {

    @Override
    protected ResultSetHandler getHandler(BeantableDao dao) {
        return dao.getBeantableListHandler();
    }
}
