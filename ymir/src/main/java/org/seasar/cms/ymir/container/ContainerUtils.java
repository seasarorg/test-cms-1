package org.seasar.cms.ymir.container;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
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

    // FIXME S2Container#findDescendantComponents()が実現されたら不要。
    public static Object[] findDescendantComponents(S2Container container,
        Object key) {

        ComponentDef[] componentDefs = findDescendantComponentDefs(container,
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

    public static ComponentDef[] findDescendantComponentDefs(
        S2Container container, Object key) {

        Set set = new LinkedHashSet();
        synchronized (container) {
            findComponentDefs(container, key, container.getRoot(), set);
        }
        return (ComponentDef[]) set.toArray(new ComponentDef[0]);
    }

    static void findComponentDefs(S2Container container, Object key,
        S2Container rootContainer, Set set) {

        if (rootContainer != null && container.getRoot() != rootContainer) {
            return;
        }
        ComponentDef[] componentDefs = container.findComponentDefs(key);
        for (int i = 0; i < componentDefs.length; i++) {
            set.add(componentDefs[i]);
        }
        int size = container.getChildSize();
        for (int i = 0; i < size; i++) {
            findComponentDefs(container.getChild(i), key, rootContainer, set);
        }
    }

    // FIXME S2Container#findAllComponents()が実現されたら不要。
    public static Object[] findAllComponents(S2Container container, Object key) {

        ComponentDef[] componentDefs = findAllComponentDefs(container, key);

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

    public static ComponentDef[] findAllComponentDefs(S2Container container,
        Object key) {

        Set set = new LinkedHashSet();
        synchronized (container) {
            findComponentDefs(container, key, null, set);
        }
        return (ComponentDef[]) set.toArray(new ComponentDef[0]);
    }
}
