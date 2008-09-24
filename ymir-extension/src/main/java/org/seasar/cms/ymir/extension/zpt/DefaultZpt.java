package org.seasar.cms.ymir.extension.zpt;

import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.TemplateEvaluator;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.impl.TemplateEvaluatorImpl;
import net.skirnir.freyja.webapp.FreyjaServlet;
import net.skirnir.freyja.webapp.ServletVariableResolverFactory;
import net.skirnir.freyja.webapp.VariableResolverFactory;
import net.skirnir.freyja.zpt.MetalTagEvaluator;
import net.skirnir.freyja.zpt.TalTagEvaluator;
import net.skirnir.freyja.zpt.webapp.ServletTalesExpressionEvaluator;

public class DefaultZpt implements Zpt {

    private TemplateEvaluator evaluator_ = new TemplateEvaluatorImpl(
            new MetalTagEvaluator(new TalTagEvaluator()),
            new ServletTalesExpressionEvaluator());

    private VariableResolverFactory vrf_ = new ServletVariableResolverFactory(
            false);

    private TemplatePathResolver templatePathResolver_ = new DefaultTemplatePathResolver();

    public TemplateEvaluator getTemplateEvaluator() {
        return evaluator_;
    }

    public TemplatePathResolver getTemplatePathResolver() {
        return templatePathResolver_;
    }

    public void buildTemplateContext(TemplateContext context,
            ServletContext servletContext, HttpServletRequest request,
            HttpServletResponse response, Locale locale, String path) {
        String responseContentType = null;
        Object obj = request
                .getAttribute(FreyjaServlet.ATTR_RESPONSECONTENTTYPE);
        if (obj != null && obj instanceof String) {
            responseContentType = (String) obj;
        }
        if (responseContentType == null
                || responseContentType.trim().length() == 0) {
            responseContentType = "text/html; charset=UTF-8";
        }

        VariableResolver resolver = null;
        obj = request.getAttribute(FreyjaServlet.ATTR_VARIABLERESOLVER);
        if (obj instanceof VariableResolver) {
            resolver = (VariableResolver) obj;
        }

        context.setProperty(TemplateContext.PROP_CONTENT_TYPE,
                responseContentType);
        context.setVariableResolver(vrf_.newResolver(request, response,
                servletContext, locale, resolver));
        context.setTemplateName(path);
    }
}
