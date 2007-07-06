package org.seasar.cms.pluggable.hotdeploy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class ProxyClassLoader extends ClassLoader {
    private ClassLoader classLoader_;

    public ProxyClassLoader(ClassLoader classLoader) {
        setClassLoader(classLoader);
    }

    public String toString() {
        return "Proxy( classLoader=" + classLoader_ + " )";
    }

    public ClassLoader getClassLoader() {
        return classLoader_;
    }

    public void setClassLoader(ClassLoader classLoader) {
        classLoader_ = classLoader;
    }

    @Override
    public URL getResource(String name) {
        return classLoader_.getResource(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return classLoader_.getResourceAsStream(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return classLoader_.getResources(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return classLoader_.loadClass(name);
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        Class clazz = classLoader_.loadClass(name);
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }
}
