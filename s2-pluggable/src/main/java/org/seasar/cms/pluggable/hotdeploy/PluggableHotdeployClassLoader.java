package org.seasar.cms.pluggable.hotdeploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.seasar.framework.container.hotdeploy.HotdeployClassLoader;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.ResourceUtil;

public class PluggableHotdeployClassLoader extends HotdeployClassLoader {

    private final ClassLoader originalClassLoader_;

    private File classesDirectory_;

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
            Class clazz = findLoadedClass(className);
            if (clazz != null) {
                return clazz;
            }
            if (classesDirectory_ != null) {
                File file = new File(classesDirectory_, className.replace('.',
                        '/').concat(".class"));
                if (file.exists()) {
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
                clazz = defineClass(className, is);
                definedClass(clazz);
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
        }
        Class clazz = originalClassLoader_.loadClass(className);
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }
}
