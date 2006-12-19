package org.seasar.cms.classbuilder.impl;

import org.aopalliance.intercept.MethodInvocation;
import org.seasar.framework.aop.interceptors.AbstractInterceptor;


public class HoeInterceptor extends AbstractInterceptor
{
    private static final long serialVersionUID = 1L;


    public Object invoke(MethodInvocation invocation)
        throws Throwable
    {
        if (invocation.getMethod().getName().equals("get")) {
            return "intercept!";
        } else {
            return invocation.proceed();
        }
    }
}
