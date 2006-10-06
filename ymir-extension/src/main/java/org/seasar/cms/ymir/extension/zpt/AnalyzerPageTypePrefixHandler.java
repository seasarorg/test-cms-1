package org.seasar.cms.ymir.extension.zpt;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.tales.TalesExpressionEvaluator;
import net.skirnir.freyja.zpt.webapp.PageTypePrefixHandler;
import net.skirnir.freyja.zpt.webapp.ServletTalesExpressionEvaluator;

public class AnalyzerPageTypePrefixHandler extends PageTypePrefixHandler {

    private ServletTalesExpressionEvaluator evaluator_;

    private PageTypePrefixHandler delegated_;

    public AnalyzerPageTypePrefixHandler(PageTypePrefixHandler delegated) {
        delegated_ = delegated;
    }

    @Override
    public void setTalesExpressionEvaluator(TalesExpressionEvaluator evaluator) {
        evaluator_ = (ServletTalesExpressionEvaluator) evaluator;
        delegated_.setTalesExpressionEvaluator(evaluator);
    }

    @Override
    public Object handle(TemplateContext context, VariableResolver varResolver,
            String expr) {
        String path = (String) delegated_.handle(context, varResolver, expr);
        String contextPath = getContextPath(evaluator_.getRequest(varResolver));
        if (path.startsWith(contextPath)) {
            int semicolon = path.indexOf(';');
            if (semicolon < 0) {
                return path.substring(contextPath.length());
            } else {
                return path.substring(contextPath.length(), semicolon);
            }
        } else {
            // 別のコンテキストのパスなので、誤解釈されないようnullにしておく。
            return null;
        }
    }
}
