package org.seasar.cms.pluggable.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.util.Traversal;

public class ContainerUtils {
    protected ContainerUtils() {
    }

    public static boolean isS23() {
        return true;
    }

    public static Object[] toComponents(Object componentKey,
            ComponentDef[] componentDefs) {
        Class<?> clazz;
        if (componentKey instanceof Class<?>) {
            clazz = (Class<?>) componentKey;
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
            final Object componentKey) {
        final List<ComponentDef> componentDefList = new ArrayList<ComponentDef>();
        Traversal.forEachContainer(container,
                new Traversal.S2ContainerHandler() {
                    public Object processContainer(S2Container container) {
                        componentDefList.addAll(Arrays
                                .asList(findLocalComponentDefs(container,
                                        componentKey)));
                        return null;
                    }
                });

        return componentDefList.toArray(new ComponentDef[0]);
    }

    public static Object[] findAllComponents(S2Container container,
            Object componentKey) {
        return toComponents(componentKey, findAllComponentDefs(container,
                componentKey));
    }

    public static HttpServletRequest getHttpServletRequest(S2Container container) {
        return container.getRequest();
    }

    public static HttpServletResponse getHttpServletResponse(
            S2Container container) {
        return container.getResponse();
    }

    public static ServletContext getServletContext(S2Container container) {
        return container.getServletContext();
    }

    public static ComponentDef[] findLocalComponentDefs(S2Container container,
            Object componentKey) {
        ComponentDef[] componentDefs = container
                .findComponentDefs(componentKey);
        if (componentDefs.length > 0
                && componentDefs[0].getContainer() != container) {
            // 見つかったComponentDefの束（親コンテナはどれも同じはず）の親が検索元のコンテナ
            // ではない場合は、ローカルにはcomponentKeyに合致するComponentDefは存在しな
            // かったということ。
            componentDefs = new ComponentDef[0];
        }
        return componentDefs;
    }

    public static Object[] findLocalComponents(S2Container container,
            Object componentKey) {
        return toComponents(componentKey, findLocalComponentDefs(container,
                componentKey));
    }

    public static void setRequest(S2Container container,
            HttpServletRequest request) {
        container.setRequest(request);
    }

    public static void setResponse(S2Container container,
            HttpServletResponse response) {
        container.setResponse(response);
    }

    public static void setServletContext(S2Container container,
            ServletContext servletContext) {
        container.setServletContext(servletContext);
    }
}
