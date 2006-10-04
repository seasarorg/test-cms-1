package org.seasar.cms.ymir.extension.creator;

import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface TemplateAnalyzer {

    void analyze(ServletContext servletContext, HttpServletRequest request,
            HttpServletResponse response, String path, String method,
            Map<String, ClassDesc> classDescriptorMap, InputStream inputStream,
            String encoding, String className);
}
