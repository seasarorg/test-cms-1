package org.seasar.cms.ymir.extension.zpt;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.tales.TalesExpressionEvaluator;
import net.skirnir.freyja.zpt.tales.TypePrefixHandler;

public class AnalyzerNotTypePrefixHandler implements TypePrefixHandler {

    private TalesExpressionEvaluator evaluator_;

    /*
     * TypePrefixHandler
     */

    public void setTalesExpressionEvaluator(TalesExpressionEvaluator evaluator) {
        evaluator_ = evaluator;
    }

    public Object handle(TemplateContext context, VariableResolver varResolver,
            String expr) {
        return Boolean.TRUE;
    }
}
