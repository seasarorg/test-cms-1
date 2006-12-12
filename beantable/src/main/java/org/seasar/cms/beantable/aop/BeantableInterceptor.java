package org.seasar.cms.beantable.aop;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.seasar.cms.beantable.Beantable;
import org.seasar.cms.beantable.BeantableDao;
import org.seasar.framework.exception.SQLRuntimeException;
import org.seasar.framework.util.MethodUtil;

abstract public class BeantableInterceptor implements MethodInterceptor {

    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        if (!MethodUtil.isAbstract(method)) {
            return invocation.proceed();
        }

        BeantableDao dao = (BeantableDao) invocation.getThis();
        Beantable beantable = dao.getBeantable();
        String query = dao.getQuery(method.getName());
        Connection con = null;
        try {
            con = beantable.getDataSource().getConnection();
            return new QueryRunner().query(con, query, invocation
                    .getArguments(), getHandler(dao));
        } catch (SQLException ex) {
            throw new SQLRuntimeException(ex);
        } finally {
            DbUtils.closeQuietly(con);
        }
    }

    abstract protected ResultSetHandler getHandler(BeantableDao dao);
}
