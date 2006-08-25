package org.seasar.cms.pluggable.hotdeploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.seasar.framework.container.hotdeploy.HotdeployClassLoader;
import org.seasar.framework.log.Logger;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.ResourceUtil;

public class PluggableHotdeployClassLoader extends HotdeployClassLoader {

    private final ClassLoader originalClassLoader_;

    private File classesDirectory_;

    private static Logger logger = Logger.getLogger(HotdeployClassLoader.class);

    public PluggableHotdeployClassLoader(ClassLoader originalClassLoader) {
        super(originalClassLoader);
        originalClassLoader_ = originalClassLoader;
    }

    public void setClassesDirectory(File classesDirectory) {
        classesDirectory_ = classesDirectory;
    }

    public Class loadClass(String className, boolean resolve)
            throws ClassNotFoundException {

        if (isTargetClass(className)) {
            if (logger.isDebugEnabled()) {
                logger.debug("isTargetClass(" + className + ") == true");
            }
            Class clazz = findLoadedClass(className);
            if (clazz != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Class " + className + " is already loaded");
                }
                return clazz;
            }
            if (classesDirectory_ != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Try to load class " + className
                            + " from classes directory '" + classesDirectory_
                            + "'");
                }
                File file = new File(classesDirectory_, className.replace('.',
                        '/').concat(".class"));
                if (file.exists()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Class resource has been found in '"
                                + classesDirectory_ + "'");
                    }
                    try {
                        clazz = defineClass(className,
                                new FileInputStream(file));
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
            InputStream is = ResourceUtil.getResourceAsStreamNoException(path);
            if (is != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Class resource has been found as '" + path
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
        if (logger.isDebugEnabled()) {
            logger.debug("Class has not been found in hotdeploy classloader");
        }
        Class clazz = originalClassLoader_.loadClass(className);
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }
}
