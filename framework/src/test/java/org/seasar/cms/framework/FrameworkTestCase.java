package org.seasar.cms.framework;

import java.io.File;

import junit.framework.TestCase;

import org.seasar.kvasir.util.io.IOUtils;

public abstract class FrameworkTestCase extends TestCase {

    protected String readResource(Class testClass, String name) {

        String className = testClass.getName();
        int dot = className.lastIndexOf('.');
        if (dot >= 0) {
            className = className.substring(dot + 1);
        }
        return IOUtils.readString(getClass().getResourceAsStream(
            className + "_" + name), "UTF-8", false);
    }

    protected File clean(File file) {
        if (!file.exists()) {
            file.mkdirs();
            return file;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                clean(files[i]);
            }
        }
        file.delete();
        return file;
    }
}
