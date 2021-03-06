package org.seasar.cms.classbuilder.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.MetaDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.AbstractS2ContainerBuilder;
import org.seasar.framework.exception.IORuntimeException;
import org.seasar.framework.util.ResourceUtil;

public class S2ContainerBuilderUtils {
    private static final String PREFIX_JAR = "jar:";

    private static final String SUFFIX_JAR = "!/";

    private static final String PREFIX_FILE = "file:";

    private static final String PREFIX_ZIP = "zip:";

    public static final String SUFFIX_ZIP = "!/";

    public static final String PREFIX_CODE_SOURCE = "code-source:";

    public static final String SUFFIX_CODE_SOURCE = "!/";

    private static final char COLON = ':';

    protected S2ContainerBuilderUtils() {
    }

    public static boolean resourceExists(String path,
            AbstractS2ContainerBuilder builder) {
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

    public static void mergeContainer(S2Container container, S2Container merged) {
        int size = merged.getChildSize();
        for (int i = 0; i < size; i++) {
            container.include(merged.getChild(i));
        }

        size = merged.getMetaDefSize();
        for (int i = 0; i < size; i++) {
            MetaDef metaDef = merged.getMetaDef(i);
            metaDef.setContainer(container);
            container.addMetaDef(metaDef);
        }

        size = merged.getComponentDefSize();
        for (int i = 0; i < size; i++) {
            ComponentDef componentDef = merged.getComponentDef(i);
            componentDef.setContainer(container);
            container.register(componentDef);
        }
    }

    public static String fromURLToResourcePath(String path) {
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
            String filePath;
            try {
                filePath = ResourceUtil.getFileName(new URL(encodeURL(path)));
            } catch (MalformedURLException ex) {
                return null;
            }

            ClassLoader cl = getClassLoader();

            int pre = 0;
            int idx;
            while ((idx = filePath.indexOf('/', pre)) >= 0) {
                pre = idx + 1;
                String resourcePath = filePath.substring(pre);
                try {
                    if (cl.getResource(resourcePath) != null) {
                        return resourcePath;
                    }
                } catch (RuntimeException ignore) {
                    // resourcePathが不正の場合はClassLoader#getResource()が
                    // IllegalArgumentExceptionなどをスローすることがあるのでこうしている。
                }
            }
            if (pre < filePath.length()) {
                String resourcePath = filePath.substring(pre);
                if (cl.getResource(resourcePath) != null) {
                    return resourcePath;
                }
            }
        } else if (path.startsWith(PREFIX_ZIP)) {
            int idx = path.indexOf(SUFFIX_ZIP);
            if (idx >= 0) {
                return path.substring(idx + SUFFIX_ZIP.length());
            } else {
                return null;
            }
        } else if (path.startsWith(PREFIX_CODE_SOURCE)) {
            int idx = path.indexOf(SUFFIX_CODE_SOURCE);
            if (idx >= 0) {
                return path.substring(idx + SUFFIX_CODE_SOURCE.length());
            } else {
                return null;
            }
        }
        return null;
    }

    public static URL[] getResourceURLs(String path) {
        return getResourceURLs(path, Thread.currentThread()
                .getContextClassLoader());
    }

    public static URL[] getResourceURLs(String path, ClassLoader classLoader) {
        if (path.indexOf(COLON) >= 0) {
            try {
                return new URL[] { new URL(path) };
            } catch (MalformedURLException ignore) {
            }
        }

        Enumeration<URL> enm;
        try {
            enm = classLoader.getResources(path);
        } catch (IOException ex) {
            throw new IORuntimeException(ex);
        }
        Set<URL> urlSet = new LinkedHashSet<URL>();
        for (; enm.hasMoreElements();) {
            urlSet.add(enm.nextElement());
        }
        return urlSet.toArray(new URL[0]);
    }

    static String encodeURL(String path) {
        if (path == null) {
            return null;
        }
        return path.replace("+", "%2B");
    }

    static ClassLoader getClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = S2ContainerBuilderUtils.class.getClassLoader();
        }
        return cl;
    }
}
