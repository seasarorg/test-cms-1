package org.seasar.cms.ymir.extension.creator;

import java.io.File;

import org.seasar.cms.ymir.Configuration;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.ResponseCreator;

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

    public boolean isDenied(String path, String method) {
        return false;
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

    public ResponseCreator getResponseCreator() {
        return null;
    }

    public boolean writeSourceFile(ClassDesc classDesc, ClassDescSet classDescSet) {
        return false;
    }

    public SourceGenerator getSourceGenerator() {
        return null;
    }

    public void writeString(String string, File file) {
    }

    public Class getClass(String className) {
        return null;
    }

    public Configuration getConfiguration() {
        return null;
    }

    public ClassDescBag gatherClassDescs(PathMetaData[] pathMetaDatas) {
        return null;
    }

    public void updateClasses(ClassDescBag classDescBag, boolean mergeMethod) {
    }
}
