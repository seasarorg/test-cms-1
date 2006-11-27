package org.seasar.cms.ymir.extension.creator.mock;

import java.io.File;
import java.io.OutputStream;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.cms.pluggable.Configuration;
import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.MatchedPathMapping;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.ResponseCreator;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.ClassDescBag;
import org.seasar.cms.ymir.extension.creator.ClassDescSet;
import org.seasar.cms.ymir.extension.creator.PathMetaData;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.SourceGenerator;
import org.seasar.cms.ymir.extension.creator.Template;
import org.seasar.cms.ymir.extension.creator.TemplateProvider;

public class MockSourceCreator implements SourceCreator {

    private ServletContext servletContext_;

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

    public MatchedPathMapping findMatchedPathMapping(String path, String method) {
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

    public Response update(Request request, Response response) {
        return null;
    }

    public File getWebappRoot() {
        return null;
    }

    public File getWebappSourceRoot() {
        return null;
    }

    public ResponseCreator getResponseCreator() {
        return null;
    }

    public boolean writeSourceFile(ClassDesc classDesc,
            ClassDescSet classDescSet) {
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

    public File getClassesDirectory() {
        return null;
    }

    public File getResourcesDirectory() {
        return null;
    }

    public Properties getSourceCreatorProperties() {
        return null;
    }

    public File getSourceDirectory() {
        return null;
    }

    public void saveSourceCreatorProperties() {
    }

    public Application getApplication() {
        return null;
    }

    public String getRootPackageName() {
        return null;
    }

    public ClassDesc newClassDesc(String className) {
        return null;
    }

    public void mergeWithExistentClass(ClassDesc desc, boolean mergeMethod) {
    }

    public Template getTemplate(String path) {
        return null;
    }

    public void writeString(String string, OutputStream os) {
    }

    public TemplateProvider getTemplateProvider() {
        return null;
    }

    public String getEncoding() {
        return "UTF-8";
    }

    public boolean shouldUpdate(Application application) {
        return false;
    }

    public String filterResponse(String response) {
        return null;
    }

    public ServletContext getServletContext() {
        return servletContext_;
    }

    public MockSourceCreator setServletContext(ServletContext servletContext) {
        servletContext_ = servletContext;
        return this;
    }

    public HttpServletRequest getHttpServletRequest() {
        return null;
    }

    public HttpServletResponse getHttpServletResponse() {
        return null;
    }
}
