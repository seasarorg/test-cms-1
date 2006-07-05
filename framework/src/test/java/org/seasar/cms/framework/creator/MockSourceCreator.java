package org.seasar.cms.framework.creator;

import java.io.File;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;

public class MockSourceCreator implements SourceCreator {

    public MockSourceCreator() {
    }

    public String getPagePackageName() {
        return null;
    }

    public String getDtoPackageName() {
        return null;
    }

    public String getDaoPackageName() {
        return null;
    }

    public String getDxoPackageName() {
        return null;
    }

    public String getComponentName(String path, String method) {
        return null;
    }

    public String getActionName(String path, String method) {
        return null;
    }

    public String getDefaultPath(String path, String method) {
        return null;
    }

    public String getClassName(String componentName) {
        return null;
    }

    public File getSourceFile(String className) {
        return null;
    }

    public File getTemplateFile(String className) {
        return null;
    }

    public Response update(String path, String method, Request request) {
        return null;
    }

    public File getWebappDirectory() {
        return null;
    }
}
