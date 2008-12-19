package org.seasar.cms.pluggable.hotdeploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.framework.container.hotdeploy.HotdeployClassLoader;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.ResourceUtil;

public class PluggableHotdeployClassLoader extends HotdeployClassLoader {

    private ClassLoader classLoader_;

    private List<HotdeployListener> listeners_ = new ArrayList<HotdeployListener>();

    private File classesDirectory_;

    private Map<String, Class<?>> classCache_ = Collections
            .synchronizedMap(new HashMap<String, Class<?>>());

    private Map<String, URL> resourceCache_ = Collections
            .synchronizedMap(new HashMap<String, URL>());

    private Map<String, Vector<URL>> resourcesCache_ = Collections
            .synchronizedMap(new HashMap<String, Vector<URL>>());

    private static Log log = LogFactory
            .getLog(PluggableHotdeployClassLoader.class);

    public PluggableHotdeployClassLoader(ClassLoader classLoader,
            NamingConvention namingConvention) {
        super(classLoader, namingConvention);
        classLoader_ = classLoader;
    }

    public void addHotdeployListener(HotdeployListener listener) {
        listeners_.add(listener);
    }

    public HotdeployListener getHotdeployListener(int index) {
        return listeners_.get(index);
    }

    public int getHotdeployListenerSize() {
        return listeners_.size();
    }

    public void removeHotdeployListener(HotdeployListener listener) {
        listeners_.remove(listener);
    }

    public void setClassesDirectory(File classesDirectory) {
        classesDirectory_ = classesDirectory;
    }

    @Override
    public URL getResource(String name) {
        if (resourceCache_.containsKey(name)) {
            return resourceCache_.get(name);
        }
        URL resource = classLoader_.getResource(name);
        resourceCache_.put(name, resource);
        return resource;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (resourcesCache_.containsKey(name)) {
            return resourcesCache_.get(name).elements();
        }
        Vector<URL> resources = new Vector<URL>();
        for (Enumeration<URL> enm = classLoader_.getResources(name); enm
                .hasMoreElements();) {
            resources.add(enm.nextElement());
        }
        resourcesCache_.put(name, resources);
        return resources.elements();
    }

    public InputStream getResourceAsStream(String name) {
        URL resource = getResource(name);
        if (resource != null) {
            try {
                return resource.openStream();
            } catch (IOException ex) {
                throw new RuntimeException(
                        "Can't open resource as stream: name=" + name
                                + ", url=" + resource, ex);
            }
        } else {
            return null;
        }
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return loadClass(className, false);
    }

    @Override
    public synchronized Class<?> loadClass(String className, boolean resolve)
            throws ClassNotFoundException {
        if (classCache_.containsKey(className)) {
            Class<?> clazz = classCache_.get(className);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            } else {
                throw new ClassNotFoundException(className);
            }
        }

        Class<?> clazz = null;
        try {
            if (isTargetClass(className)) {
                if (log.isDebugEnabled()) {
                    log.debug("isTargetClass(" + className + ") == true");
                }
                clazz = findLoadedClass(className);
                if (clazz != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Class " + className + " is already loaded");
                    }
                    return clazz;
                }
                if (classesDirectory_ != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Try to load class " + className
                                + " from classes directory '"
                                + classesDirectory_ + "'");
                    }
                    File file = new File(classesDirectory_, className.replace(
                            '.', '/').concat(".class"));
                    if (file.exists()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Class resource has been found in '"
                                    + classesDirectory_ + "'");
                        }
                        try {
                            clazz = defineClass(className, new FileInputStream(
                                    file));
                            definedClass(clazz);
                            if (resolve) {
                                resolveClass(clazz);
                            }
                            return clazz;
                        } catch (FileNotFoundException ex) {
                        }
                    }
                }
                String path = ClassUtil.getResourcePath(className);
                InputStream is = ResourceUtil
                        .getResourceAsStreamNoException(path);
                if (is != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Class resource has been found as '" + path
                                + "'");
                    }
                    clazz = defineClass(className, is);
                    definedClass(clazz);
                    if (resolve) {
                        resolveClass(clazz);
                    }
                    return clazz;
                }
            }
            clazz = classLoader_.loadClass(className);
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        } finally {
            classCache_.put(className, clazz);
        }
    }

    protected void definedClass(Class<?> clazz) {
        final int listenerSize = getHotdeployListenerSize();
        for (int i = 0; i < listenerSize; ++i) {
            HotdeployListener listener = getHotdeployListener(i);
            listener.definedClass(clazz);
        }
    }
}
