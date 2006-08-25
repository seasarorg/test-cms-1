package org.seasar.cms.pluggable.aop.interceptors;

import org.aopalliance.intercept.MethodInvocation;
import org.seasar.framework.aop.interceptors.AbstractInterceptor;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.log.Logger;

public class TraceS2Interceptor extends AbstractInterceptor {

    private static final long serialVersionUID = 2041230911096214258L;

    private static final String METHOD_ACQUIRE_FROM_GET_COMPONENT = "acquireFromGetComponent";

    private static Logger logger = Logger.getLogger(TraceS2Interceptor.class);

    public Object invoke(MethodInvocation invocation) throws Throwable {

        if (!isSuitable(invocation)) {
            return invocation.proceed();
        }

        S2Container container = (S2Container) invocation.getArguments()[0];
        Object key = invocation.getArguments()[1];

        StringBuffer sb = new StringBuffer("[TRACE] getComponent(");
        appendStringRepresentation(key, sb).append(") from ").append(
                container.getPath());
        if (logger.isDebugEnabled()) {
            logger.debug(sb.toString());
        }

        Object ret = null;
        Throwable cause = null;
        sb = new StringBuffer("[TRACE]   -> ");
        try {
            ret = invocation.proceed();
            appendStringRepresentation(ret, sb);
        } catch (Throwable t) {
            sb.append(" (Exception occured: ").append(t.getClass().getName())
                    .append(")");
            cause = t;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(sb.toString());
        }
        if (cause == null) {
            return ret;
        }
        throw cause;
    }

    boolean isSuitable(MethodInvocation invocation) {

        if (!invocation.getMethod().getName().equals(
                METHOD_ACQUIRE_FROM_GET_COMPONENT)) {
            return false;
        }
        Object[] args = invocation.getArguments();
        if (args == null || args.length != 2
                || !(args[0] instanceof S2Container)) {
            return false;
        }

        return true;
    }

    StringBuffer appendStringRepresentation(Object obj, StringBuffer sb) {

        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof String) {
            sb.append('"').append(obj).append('"');
        } else if (obj instanceof Class) {
            sb.append(((Class) obj).getName()).append(".class");
        } else if (obj instanceof ComponentDef) {
            ComponentDef componentDef = (ComponentDef) obj;
            appendStringRepresentation(componentDef.getComponent(), sb).append(
                    " (from componentDef(name=").append(
                    componentDef.getComponentName()).append(", class=").append(
                    getClassNameSafely(componentDef.getComponentClass()))
                    .append(", belongs to \"").append(
                            componentDef.getContainer().getPath())
                    .append("\")");
        } else {
            sb.append(obj);
        }
        return sb;
    }

    String getClassNameSafely(Class clazz) {
        return (clazz != null ? clazz.getName() : null);
    }
}
