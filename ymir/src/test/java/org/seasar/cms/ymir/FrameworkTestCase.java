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

    protected File clean(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
            return dir;
        } else {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                clean0(files[i]);
            }
        }
        return dir;
    }

    File clean0(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                clean0(files[i]);
            }
        }
        file.delete();
        return file;
    }
}
