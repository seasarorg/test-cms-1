package org.seasar.cms.beantable.aop;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.seasar.cms.beantable.BeantableDao;
import org.seasar.framework.aop.interceptors.AbstractInterceptor;
import org.seasar.framework.util.MethodUtil;

public class BeantableDaoInterceptor extends AbstractInterceptor {

    private static final long serialVersionUID = -5851647803534651222L;

    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        if (!MethodUtil.isAbstract(method)) {
            return invocation.proceed();
        }

        return ((BeantableDao) invocation.getThis()).execute(method.getName(),
                invocation.getArguments(), method.getParameterTypes(), method
                        .getReturnType());
    }
}
