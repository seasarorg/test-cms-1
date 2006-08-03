package org.seasar.cms.pluggable.hotdeploy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;


public class ProxyClassLoader extends ClassLoader
{
    private ClassLoader classLoader_;


    public ProxyClassLoader(ClassLoader classLoader)
    {
        setClassLoader(classLoader);
    }


    public ClassLoader getClassLoader()
    {
        return classLoader_;
    }


    public void setClassLoader(ClassLoader classLoader)
    {
        classLoader_ = classLoader;
    }


    public URL getResource(String name)
    {
        return classLoader_.getResource(name);
    }


    public InputStream getResourceAsStream(String name)
    {
        return classLoader_.getResourceAsStream(name);
    }


    public Enumeration getResources(String name)
        throws IOException
    {
        return classLoader_.getResources(name);
    }


    public Class loadClass(String name)
        throws ClassNotFoundException
    {
        return classLoader_.loadClass(name);
    }
}
