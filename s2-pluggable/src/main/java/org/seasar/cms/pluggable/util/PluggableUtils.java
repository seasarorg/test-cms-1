package org.seasar.cms.pluggable.util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.util.Traversal;
import org.seasar.framework.exception.IORuntimeException;

public class PluggableUtils {

    private static final String EMPTY_DICON = "empty.dicon";

    private static final String PATH_PREFIX = "unique:";

    private static int id;

    private PluggableUtils() {
    }

    public static URL[] getResourceURLs(String path) {

        return getResourceURLs(path, Thread.currentThread()
                .getContextClassLoader());
    }

    public static URL[] getResourceURLs(String path, ClassLoader classLoader) {

        if (classLoader == null) {
            classLoader = PluggableUtils.class.getClassLoader();
        }
        Enumeration enm;
        try {
            enm = classLoader.getResources(path);
        } catch (IOException ex) {
            throw new IORuntimeException(ex);
        }
        Set urlSet = new LinkedHashSet();
        for (; enm.hasMoreElements();) {
            urlSet.add(enm.nextElement());
        }
        return (URL[]) urlSet.toArray(new URL[0]);
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

    public static S2Container newContainer() {
        S2Container container = S2ContainerFactory.create(EMPTY_DICON,
                PluggableUtils.class.getClassLoader());
        container.setPath(newPath());
        return container;
    }

    static synchronized String newPath() {
        return PATH_PREFIX + String.valueOf(++id);
    }
}
