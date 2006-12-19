package org.seasar.cms.classbuilder.util;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * 
 * <p><b>同期化：</b>
 * このクラスはスレッドセーフです。</p>
 * 
 * @author YOKOTA Takehiko
 */
public class CompositeClassLoader extends ClassLoader
{
    private ClassLoader[] classLoaders_;


    /*
     * constructors
     */

    public CompositeClassLoader(ClassLoader[] classLoaders)
    {
        super(getSystemClassLoader().getParent());
        classLoaders_ = normalizeClassLoaders(classLoaders);
    }


    public CompositeClassLoader(ClassLoader[] classLoaders, ClassLoader parent)
    {
        super(parent);
        classLoaders_ = normalizeClassLoaders(classLoaders);
    }


    ClassLoader[] normalizeClassLoaders(ClassLoader[] classLoaders)
    {
        Set<ClassLoader> set = new LinkedHashSet<ClassLoader>();
        for (int i = 0; i < classLoaders.length; i++) {
            if (set.contains(classLoaders[i])) {
                continue;
            }
            set.add(classLoaders[i]);
        }
        return set.toArray(new ClassLoader[0]);
    }


    /*
     * Object
     */

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("C{ ");
        for (int i = 0; i < classLoaders_.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(classLoaders_[i]);
        }
        sb.append(" }(parent=");
        sb.append(getParent());
        sb.append(")");
        return sb.toString();
    }


    /*
     * OverriddenClassLoader
     */

    public URL getResource(String name)
    {
        for (int i = 0; i < classLoaders_.length; i++) {
            URL url = classLoaders_[i].getResource(name);
            if (url != null) {
                return url;
            }
        }
        if (getParent() != null) {
            return super.getResource(name);
        } else {
            return null;
        }
    }


    public Enumeration<URL> getResources(String name)
        throws IOException
    {
        Set<URL> set = new LinkedHashSet<URL>();
        for (int i = 0; i < classLoaders_.length; i++) {
            for (Enumeration<URL> enm = classLoaders_[i].getResources(name); enm
                .hasMoreElements();) {
                set.add(enm.nextElement());
            }
        }
        if (getParent() != null) {
            for (Enumeration<URL> enm = super.getResources(name); enm
                .hasMoreElements();) {
                set.add(enm.nextElement());
            }
        }
        return Collections.enumeration(set);
    }


    public synchronized Class<?> loadClass(String name)
        throws ClassNotFoundException
    {
        for (int i = 0; i < classLoaders_.length; i++) {
            try {
                return classLoaders_[i].loadClass(name);
            } catch (ClassNotFoundException ex) {
                ;
            }
        }
        if (getParent() != null) {
            return super.loadClass(name);
        } else {
            throw new ClassNotFoundException("Class doesn't exist: " + name);
        }
    }
}
