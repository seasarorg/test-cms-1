package org.seasar.cms.ymir.container;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.util.Traversal;
import org.seasar.framework.exception.IORuntimeException;

public class ContainerUtils {

    private ContainerUtils() {
    }

    public static URL[] getResourceURLs(String path) {

        Enumeration enm;
        try {
            enm = getClassLoader().getResources(path);
        } catch (IOException ex) {
            throw new IORuntimeException(ex);
        }
        List list = new ArrayList();
        for (; enm.hasMoreElements();) {
            list.add(enm.nextElement());
        }
        return (URL[]) list.toArray(new URL[0]);
    }

    public static ClassLoader getClassLoader() {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ContainerUtils.class.getClassLoader();
        }
        return cl;
    }

    public static Object[] findAscendantComponents(S2Container container,
        Object key) {

        ComponentDef[] componentDefs = findAscendantComponentDefs(container,
            key);

        Class clazz;
        if (key instanceof Class) {
            clazz = (Class) key;
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

    public static ComponentDef[] findAscendantComponentDefs(
        final S2Container container, final Object componentKey) {

        synchronized (container.getRoot()) {
            final List componentDefs = new ArrayList();
            Traversal.forEachParentContainer(container,
                new Traversal.S2ContainerHandler() {
                    public Object processContainer(S2Container container) {
                        componentDefs.addAll(Arrays.asList(container
                            .findLocalComponentDefs(componentKey)));
                        return null;
                    }
                });
            return (ComponentDef[]) componentDefs.toArray(new ComponentDef[0]);
        }
    }
}
