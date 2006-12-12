package org.seasar.cms.beantable.aop;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.seasar.cms.beantable.BeantableDao;

public class ScalarBeantableInterceptor extends BeantableInterceptor {

    private static final ResultSetHandler HANDLER = new ScalarHandler();

    @Override
    protected ResultSetHandler getHandler(BeantableDao dao) {
        return HANDLER;
    }
}
