package org.seasar.cms.pluggable.util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.seasar.cms.pluggable.hotdeploy.DistributedHotdeployBehavior;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.util.Traversal;
import org.seasar.framework.container.util.Traversal24;
import org.seasar.framework.exception.IORuntimeException;

public class PluggableUtils {

    private static final String EMPTY_DICON = "empty.dicon";

    private static final String PATH_PREFIX = "unique:";

    private static final char COLON = ':';

    private static int id;

    private PluggableUtils() {
    }

    public static URL[] getResourceURLs(String path) {

        return getResourceURLs(path, Thread.currentThread()
                .getContextClassLoader());
    }

    public static URL[] getResourceURLs(String path, ClassLoader classLoader) {

        if (path.indexOf(COLON) >= 0) {
            try {
                return new URL[] { new URL(path) };
            } catch (MalformedURLException ex) {
                return new URL[0];
            }
        } else {
            if (classLoader == null) {
                classLoader = PluggableUtils.class.getClassLoader();
            }
            Enumeration<URL> enm;
            try {
                enm = classLoader.getResources(path);
            } catch (IOException ex) {
                throw new IORuntimeException(ex);
            }
            Set<URL> urlSet = new LinkedHashSet<URL>();
            for (; enm.hasMoreElements();) {
                urlSet.add(enm.nextElement());
            }
            return urlSet.toArray(new URL[0]);
        }
    }

    public static Object[] findAscendantComponents(S2Container container,
            Object key) {

        ComponentDef[] componentDefs = findAscendantComponentDefs(container,
                key);

        Class<?> clazz;
        if (key instanceof Class<?>) {
            clazz = (Class<?>) key;
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
            final List<ComponentDef> componentDefs = new ArrayList<ComponentDef>();
            Traversal24.forEachParentContainer(container,
                    new Traversal.S2ContainerHandler() {
                        public Object processContainer(S2Container container) {
                            componentDefs.addAll(Arrays.asList(ContainerUtils
                                    .findLocalComponentDefs(container,
                                            componentKey)));
                            return null;
                        }
                    });
            return componentDefs.toArray(new ComponentDef[0]);
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

    public static ClassLoader adjustClassLoader(
            DistributedHotdeployBehavior behavior, ClassLoader parent) {
        return parent;
    }
}
