package org.seasar.cms.ymir.extension.zpt;

import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.TemplateEvaluator;

public interface Zpt {

    TemplateEvaluator getTemplateEvaluator();

    TemplatePathResolver getTemplatePathResolver();

    void buildTemplateContext(TemplateContext context,
            ServletContext servletContext, HttpServletRequest request,
            HttpServletResponse response, Locale locale, String path);
}
