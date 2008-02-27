package org.seasar.cms.pluggable.util;

import java.lang.reflect.Array;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;

public class ContainerUtils {
    protected ContainerUtils() {
    }

    public static boolean isS23() {
        return false;
    }

    public static Object[] toComponents(Object componentKey,
            ComponentDef[] componentDefs) {
        Class clazz;
        if (componentKey instanceof Class) {
            clazz = (Class) componentKey;
        } else {
            clazz = Object.class;
        }
        Object[] objs = (Object[]) Array.newInstance(clazz,
                componentDefs.length);
        for (int i = 0; i < objs.length; i++) {
            objs[i] = componentDefs[i].getComponent();
        }
        return objs;
    }

    public static ComponentDef[] findAllComponentDefs(S2Container container,
            Object componentKey) {
        return container.findAllComponentDefs(componentKey);
    }

    public static Object[] findAllComponents(S2Container container,
            Object componentKey) {
        return container.findAllComponents(componentKey);
    }

    public static HttpServletRequest getHttpServletRequest(S2Container container) {
        return (HttpServletRequest) container.getExternalContext().getRequest();
    }

    public static HttpServletResponse getHttpServletResponse(
            S2Container container) {
        return (HttpServletResponse) container.getExternalContext()
                .getResponse();
    }

    public static ServletContext getServletContext(S2Container container) {
        return (ServletContext) container.getExternalContext().getApplication();
    }

    public static ComponentDef[] findLocalComponentDefs(S2Container container,
            Object componentKey) {
        return container.findLocalComponentDefs(componentKey);
    }

    public static Object[] findLocalComponents(S2Container container,
            Object componentKey) {
        return container.findLocalComponents(componentKey);
    }

    public static void setRequest(S2Container container,
            HttpServletRequest request) {
        container.getExternalContext().setRequest(request);
    }

    public static void setResponse(S2Container container,
            HttpServletResponse response) {
        container.getExternalContext().setResponse(response);
    }

    public static void setServletContext(S2Container container,
            ServletContext servletContext) {
        container.getExternalContext().setApplication(servletContext);
    }
}
