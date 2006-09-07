package org.seasar.cms.pluggable.hotdeploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.seasar.framework.container.hotdeploy.HotdeployClassLoader;
import org.seasar.framework.log.Logger;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.ResourceUtil;

public class PluggableHotdeployClassLoader extends HotdeployClassLoader {

    private ClassLoader classLoader_;

    private File classesDirectory_;

    private Map classCache_ = Collections.synchronizedMap(new HashMap());

    private Map resourceCache_ = Collections.synchronizedMap(new HashMap());

    private Map resourcesCache_ = Collections.synchronizedMap(new HashMap());

    private static Logger logger = Logger.getLogger(HotdeployClassLoader.class);

    public PluggableHotdeployClassLoader(ClassLoader classLoader) {
        super(classLoader);
        classLoader_ = classLoader;
    }

    public void setClassesDirectory(File classesDirectory) {
        classesDirectory_ = classesDirectory;
    }

    public URL getResource(String name) {
        if (resourceCache_.containsKey(name)) {
            return (URL) resourceCache_.get(name);
        }
        URL resource = classLoader_.getResource(name);
        resourceCache_.put(name, resource);
        return resource;
    }

    public Enumeration getResources(String name) throws IOException {
        if (resourcesCache_.containsKey(name)) {
            return ((Vector) resourcesCache_.get(name)).elements();
        }
        Vector resources = new Vector();
        for (Enumeration enm = classLoader_.getResources(name); enm
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

    public Class loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    public synchronized Class loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        if (classCache_.containsKey(name)) {
            Class clazz = (Class) classCache_.get(name);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            } else {
                throw new ClassNotFoundException(name);
            }
        }
        Class clazz = null;
        try {
            if (isTargetClass(name)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("isTargetClass(" + name + ") == true");
                }
                clazz = findLoadedClass(name);
                if (clazz != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Class " + name + " is already loaded");
                    }
                    return clazz;
                }
                if (classesDirectory_ != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Try to load class " + name
                                + " from classes directory '"
                                + classesDirectory_ + "'");
                    }
                    File file = new File(classesDirectory_, name.replace('.',
                            '/').concat(".class"));
                    if (file.exists()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Class resource has been found in '"
                                    + classesDirectory_ + "'");
                        }
                        try {
                            clazz = defineClass(name, new FileInputStream(file));
                            definedClass(clazz);
                            if (resolve) {
                                resolveClass(clazz);
                            }
                            return clazz;
                        } catch (FileNotFoundException ex) {
                        }
                    }
                }
                String path = ClassUtil.getResourcePath(name);
                InputStream is = ResourceUtil
                        .getResourceAsStreamNoException(path);
                if (is != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Class resource has been found as '"
                                + path + "'");
                    }
                    clazz = defineClass(name, is);
                    definedClass(clazz);
                    if (resolve) {
                        resolveClass(clazz);
                    }
                    return clazz;
                }
            }
            if (logger.isDebugEnabled()) {
                logger
                        .debug("Class has not been found in hotdeploy classloader");
            }
            clazz = classLoader_.loadClass(name);
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        } finally {
            classCache_.put(name, clazz);
        }
    }
}
