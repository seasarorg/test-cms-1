package org.seasar.cms.ymir.extension.zpt;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.ResponseCreator;
import org.seasar.cms.ymir.impl.SelfContainedResponse;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.TemplateEvaluator;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.impl.VariableResolverImpl;
import net.skirnir.freyja.zpt.MetalTagEvaluator;
import net.skirnir.freyja.zpt.tales.TalesExpressionEvaluator;

public class ZptResponseCreator implements ResponseCreator {

    private static final String TEMPLATE_PREFIX = "template/";

    private static final String TEMPLATE_SUFFIX = ".zpt";

    private TemplateEvaluator evaluator_ = new TemplateEvaluator(
        new MetalTagEvaluator(), new TalesExpressionEvaluator());

    public Response createResponse(String path, Map variableMap) {

        TemplateContext context = evaluator_.newContext();
        context.setProperty(TemplateContext.PROP_CONTENT_TYPE, "text/html");
        if (variableMap != null) {
            VariableResolver resolver = new VariableResolverImpl();
            for (Iterator itr = variableMap.entrySet().iterator(); itr
                .hasNext();) {
                Map.Entry entry = (Map.Entry) itr.next();
                resolver.setVariable((String) entry.getKey(), entry.getValue());
            }
            context.setVariableResolver(resolver);
        }
        try {
            return new SelfContainedResponse(evaluator_.evaluate(context,
                new InputStreamReader(getClass().getResourceAsStream(
                    TEMPLATE_PREFIX + path + TEMPLATE_SUFFIX), "UTF-8")));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
