package org.seasar.cms.pluggable.hotdeploy;

import java.io.InputStream;

import org.seasar.framework.container.hotdeploy.HotdeployClassLoader;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.InputStreamUtil;

/*
 * FIXME s2-container-2.4.0-beta-5以降は不要。
 */
public class TemporaryHotdeployClassLoader extends HotdeployClassLoader {

    public TemporaryHotdeployClassLoader(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public Class loadClass(String className, boolean resolve)
            throws ClassNotFoundException {

        if (isTargetClass(className)) {
            Class clazz = findLoadedClass(className);
            if (clazz != null) {
                return clazz;
            }
            String path = ClassUtil.getResourcePath(className);
            InputStream is = getResourceAsStream(path);
            if (is != null) {
                clazz = defineClass(className, is);
                definedClass(clazz);
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
        }
        return super.loadClass(className, resolve);
    }

    protected Class defineClass(String className, InputStream classFile) {
        return defineClass(className, InputStreamUtil.getBytes(classFile));
    }
}
