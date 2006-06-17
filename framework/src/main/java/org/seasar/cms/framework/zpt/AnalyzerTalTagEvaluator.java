package org.seasar.cms.framework.zpt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.seasar.cms.framework.creator.MethodDesc;
import org.seasar.cms.framework.creator.PropertyDesc;
import org.seasar.cms.framework.creator.SourceCreator;

import net.skirnir.freyja.Attribute;
import net.skirnir.freyja.Element;
import net.skirnir.freyja.EvaluationException;
import net.skirnir.freyja.ExpressionEvaluator;
import net.skirnir.freyja.TagEvaluatorUtils;
import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.TalTagEvaluator;
import net.skirnir.freyja.zpt.ZptTemplateContext;

public class AnalyzerTalTagEvaluator extends TalTagEvaluator {

    public String evaluate(TemplateContext context, String name,
        Attribute[] attrs, Element[] body) {

        AnnotationResult result = findAnnotation(context, name, attrs);
        String annotation = result.getAnnotation();
        attrs = result.getTheOtherAttributes();

        if ("form".equals(name)) {
            AnalyzerContext analyzeContext = toAnalyzeContext(context);
            SourceCreator creator = analyzeContext.getSourceCreator();
            Map attrMap = evaluate(analyzeContext, attrs);
            String action = getAttributeValue(attrMap, "action", null);
            String method = getAttributeValue(attrMap, "method", "GET")
                .toUpperCase();
            String className = creator.getClassName(creator.getComponentName(
                action, method));
            String actionName = creator.getActionName(action, method);
            if (actionName != null) {
                analyzeContext.getClassDesc(className).setMethodDesc(
                    new MethodDesc(actionName));
            }

            analyzeContext.setFormActionPageClassName(className);
            try {
                return super.evaluate(context, name, attrs, body);
            } finally {
                analyzeContext.setFormActionPageClassName(null);
            }
        } else if ("input".equals(name) || "select".equals(name)
            || "textarea".equals(name)) {

            processParameterTag(toAnalyzeContext(context), attrs, annotation);
        }

        return super.evaluate(context, name, attrs, body);
    }

    AnnotationResult findAnnotation(TemplateContext context, String name,
        Attribute[] attrs) {

        String behaviorDuplicateTag = context
            .getProperty("behavior.duplicate-tag");
        String annotation = null;
        List attrList = new ArrayList();
        for (int i = 0; i < attrs.length; i++) {
            if ("tal:annotation".equals(attrs[i].getName())) {
                if (annotation != null) {
                    if (!"ignore".equals(behaviorDuplicateTag)) {
                        throw new EvaluationException("Duplicate tag found: "
                            + attrs[i].getName() + ": "
                            + TagEvaluatorUtils.getBeginTagString(name, attrs));
                    }
                }
                annotation = attrs[i].getValue();
            } else {
                attrList.add(attrs[i]);
            }
        }
        return new AnnotationResult(annotation, (Attribute[]) attrList
            .toArray(new Attribute[0]));
    }

    void processParameterTag(AnalyzerContext context, Attribute[] attrs,
        String annotation) {

        String className = context.getFormActionPageClassName();
        if (className == null) {
            return;
        }

        Map attrMap = evaluate(context, attrs);
        Attribute attr = (Attribute) attrMap.get("name");
        if (attr != null) {
            context.getClassDesc(className)
                .addProperty(TagEvaluatorUtils.defilter(attr.getValue()),
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
        if (attributesAttr != null) {
            attrs = processAttributes(context, expEvaluator, varResolver,
                attributesAttr, attrs, true);
        }
        return TagEvaluatorUtils.toMap(attrs);
    }

    AnalyzerContext toAnalyzeContext(TemplateContext context) {

        return (AnalyzerContext) context;
    }

    String getAttributeValue(Map attrMap, String name, String defaultValue) {

        Attribute attr = (Attribute) attrMap.get(name);
        if (attr != null) {

            return TagEvaluatorUtils.defilter(attr.getValue());
        } else {
            return defaultValue;
        }
    }

    static class AnnotationResult {

        private String annotation_;

        private Attribute[] theOtherAttributes_;

        public AnnotationResult(String annotation,
            Attribute[] theOtherAttributes) {
            annotation_ = annotation;
            theOtherAttributes_ = theOtherAttributes;
        }

        public String getAnnotation() {
            return annotation_;
        }

        public Attribute[] getTheOtherAttributes() {
            return theOtherAttributes_;
        }
    }
}
