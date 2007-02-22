package org.seasar.cms.ymir.extension.creator;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface TemplateAnalyzer {

    void analyze(ServletContext servletContext, HttpServletRequest request,
            HttpServletResponse response, String path, String method,
            Map<String, ClassDesc> classDescriptorMap, Template template,
            String className, String[] ignoreVariables);
}
