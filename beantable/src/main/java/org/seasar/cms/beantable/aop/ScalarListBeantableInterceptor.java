package org.seasar.cms.beantable.aop;

import org.apache.commons.dbutils.ResultSetHandler;
import org.seasar.cms.beantable.BeantableDao;
import org.seasar.cms.beantable.handler.ScalarListHandler;

public class ScalarListBeantableInterceptor extends BeantableInterceptor {

    private static final ResultSetHandler HANDLER = new ScalarListHandler();

    @Override
    protected ResultSetHandler getHandler(BeantableDao dao) {
        return HANDLER;
    }
}
