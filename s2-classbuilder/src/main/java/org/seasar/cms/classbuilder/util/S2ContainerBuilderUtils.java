package org.seasar.cms.classbuilder.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.AbstractS2ContainerBuilder;
import org.seasar.framework.exception.IORuntimeException;
import org.seasar.framework.util.ResourceUtil;


public class S2ContainerBuilderUtils
{
    private static final String PREFIX_JAR = "jar:";

    private static final String SUFFIX_JAR = "!/";

    private static final String PREFIX_FILE = "file:";


    protected S2ContainerBuilderUtils()
    {
    }


    public static boolean resourceExists(String path,
        AbstractS2ContainerBuilder builder)
    {
        InputStream is;
        try {
            is = builder.getResourceResolver().getInputStream(path);
        } catch (IORuntimeException ex) {
            if (ex.getCause() instanceof FileNotFoundException) {
                return false;
            } else {
                throw ex;
            }
        }
        if (is == null) {
            return false;
        } else {
            try {
                is.close();
            } catch (IOException ignore) {
            }
            return true;
        }
    }


    public static void mergeContainer(S2Container container, S2Container merged)
    {
        int size = merged.getChildSize();
        for (int i = 0; i < size; i++) {
            container.include(merged.getChild(i));
        }

        size = merged.getMetaDefSize();
        for (int i = 0; i < size; i++) {
            container.addMetaDef(merged.getMetaDef(i));
        }

        size = merged.getComponentDefSize();
        for (int i = 0; i < size; i++) {
            container.register(merged.getComponentDef(i));
        }
    }


    public static String fromURLToResourcePath(String path)
    {
        if (path == null) {
            return null;
        }
        if (path.startsWith(PREFIX_JAR)) {
            int idx = path.indexOf(SUFFIX_JAR);
            if (idx >= 0) {
                return path.substring(idx + SUFFIX_JAR.length());
            } else {
                return null;
            }
        } else if (path.startsWith(PREFIX_FILE)) {
            File file;
            try {
                file = ResourceUtil.getFile(new URL(path));
            } catch (MalformedURLException ex) {
                return null;
            }
            if (file == null) {
                return null;
            }

            String filePath;
            try {
                filePath = file.getCanonicalPath();
            } catch (IOException ex) {
                filePath = file.getAbsolutePath();
            }

            ClassLoader cl = getClassLoader();

            int pre = 0;
            int idx;
            while ((idx = filePath.indexOf(File.separatorChar, pre)) >= 0) {
                pre = idx + 1;
                String resourcePath = filePath.substring(pre);
                if (cl.getResource(resourcePath) != null) {
                    return resourcePath;
                }
            }
            if (pre < filePath.length()) {
                String resourcePath = filePath.substring(pre);
                if (cl.getResource(resourcePath) != null) {
                    return resourcePath;
                }
            }
        }
        return null;
    }


    static ClassLoader getClassLoader()
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = S2ContainerBuilderUtils.class.getClassLoader();
        }
        return cl;
    }
}
