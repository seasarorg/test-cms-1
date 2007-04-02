package org.seasar.cms.classbuilder.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.AbstractS2ContainerBuilder;
import org.seasar.framework.exception.IORuntimeException;


public class S2ContainerBuilderUtils
{
    private static final String PREFIX_JAR = "jar:";

    private static final String SUFFIX_JAR = "!/";


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


    public static String fromJarURLToResourcePath(String path)
    {
        if (path == null) {
            return null;
        }
        if (!path.startsWith(PREFIX_JAR)) {
            return null;
        }
        int idx = path.indexOf(SUFFIX_JAR);
        if (idx >= 0) {
            return path.substring(idx + SUFFIX_JAR.length());
        } else {
            return null;
        }
    }
}
