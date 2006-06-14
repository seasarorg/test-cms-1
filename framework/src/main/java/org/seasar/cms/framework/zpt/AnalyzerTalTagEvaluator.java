package org.seasar.cms.framework.zpt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.seasar.cms.framework.creator.PropertyDesc;
import org.seasar.cms.framework.creator.SourceCreator;

import net.skirnir.freyja.Attribute;
import net.skirnir.freyja.Element;
import net.skirnir.freyja.ExpressionEvaluator;
import net.skirnir.freyja.TagEvaluatorUtils;
import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.TalTagEvaluator;
import net.skirnir.freyja.zpt.ZptTemplateContext;

public class AnalyzerTalTagEvaluator extends TalTagEvaluator {

    public String evaluate(TemplateContext context, String name,
        Attribute[] attrs, Element[] body) {

        if ("form".equals(name)) {
            AnalyzerContext analyzeContext = toAnalyzeContext(context);
            analyzeContext.setFormActionPageClassName(getActionClassName(
                analyzeContext, attrs));
            try {
                return super.evaluate(context, name, attrs, body);
            } finally {
                analyzeContext.setFormActionPageClassName(null);
            }
        }

        if ("input".equals(name)) {
            processInput(toAnalyzeContext(context), attrs);
        }

        return super.evaluate(context, name, attrs, body);
    }

    void processInput(AnalyzerContext context, Attribute[] attrs) {

        String className = context.getFormActionPageClassName();
        if (className == null) {
            return;
        }

        Map attrMap = evaluate(context, attrs);
        Attribute attr = (Attribute) attrMap.get("name");
        if (attr != null) {
            context.getClassDescriptor(className).addProperty(
                TagEvaluatorUtils.defilter(attr.getValue()),
                PropertyDesc.WRITE);
        }
    }

    Map evaluate(ZptTemplateContext context, Attribute[] attrs) {

        ExpressionEvaluator expEvaluator = context.getExpressionEvaluator();
        VariableResolver varResolver = context.getVariableResolver();

        List attrList = new ArrayList();
        Attribute attributesAttr = null;
        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i].getName().startsWith("tal:")) {
                if ("tal:attributes".equals(attrs[i].getName())) {
                    attributesAttr = attrs[i];
                }
            } else {
                attrList.add(attrs[i]);
            }
        }
        return TagEvaluatorUtils.toMap(processAttributes(context, expEvaluator,
            varResolver, attributesAttr, attrs, true));
    }

    String getActionClassName(AnalyzerContext context, Attribute[] attrs) {

        Map attrMap = evaluate(context, attrs);
        Attribute actionAttr = (Attribute) attrMap.get("action");
        if (actionAttr == null) {
            return null;
        } else {
            SourceCreator creator = context.getSourceCreator();
            return creator.getClassName(creator
                .getComponentName(TagEvaluatorUtils.defilter(actionAttr
                    .getValue())));
        }
    }

    AnalyzerContext toAnalyzeContext(TemplateContext context) {

        return (AnalyzerContext) context;
    }
}
