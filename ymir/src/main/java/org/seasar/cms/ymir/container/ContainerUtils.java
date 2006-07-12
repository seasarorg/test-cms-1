package org.seasar.cms.ymir.container;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.seasar.framework.exception.IORuntimeException;

public class ContainerUtils {

    private ContainerUtils() {
    }

    public static URL[] getResourceURLs(String path) {

        Enumeration enm;
        try {
            enm = getClassLoader().getResources(path);
        } catch (IOException ex) {
            throw new IORuntimeException(ex);
        }
        List list = new ArrayList();
        for (; enm.hasMoreElements();) {
            list.add(enm.nextElement());
        }
        return (URL[]) list.toArray(new URL[0]);
    }

    public static ClassLoader getClassLoader() {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ContainerUtils.class.getClassLoader();
        }
        return cl;
    }
}
