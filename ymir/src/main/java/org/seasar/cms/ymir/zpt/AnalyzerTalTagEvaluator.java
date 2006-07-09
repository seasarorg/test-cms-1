package org.seasar.cms.framework.zpt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.skirnir.freyja.Attribute;
import net.skirnir.freyja.Element;
import net.skirnir.freyja.EvaluationException;
import net.skirnir.freyja.ExpressionEvaluator;
import net.skirnir.freyja.TagEvaluatorUtils;
import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.TalTagEvaluator;
import net.skirnir.freyja.zpt.ZptTemplateContext;

import org.seasar.cms.framework.FormFile;
import org.seasar.cms.framework.Path;
import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.PropertyDesc;
import org.seasar.cms.framework.creator.SourceCreator;
import org.seasar.cms.framework.creator.impl.MethodDescImpl;

public class AnalyzerTalTagEvaluator extends TalTagEvaluator {

    public String evaluate(TemplateContext context, String name,
        Attribute[] attrs, Element[] body) {

        AnnotationResult result = findAnnotation(context, name, attrs);
        String annotation = result.getAnnotation();
        attrs = result.getTheOtherAttributes();
        AnalyzerContext analyzeContext = toAnalyzeContext(context);
        Map attrMap = evaluate(analyzeContext, attrs);

        if ("form".equals(name)) {
            ClassDesc classDesc = registerClassDesc(analyzeContext, attrMap,
                "action", getAttributeValue(attrMap, "method", "GET")
                    .toUpperCase());
            analyzeContext
                .setFormActionPageClassName((classDesc != null ? classDesc
                    .getName() : null));
            try {
                return super.evaluate(context, name, attrs, body);
            } finally {
                analyzeContext.setFormActionPageClassName(null);
            }
        } else if ("input".equals(name)) {
            String type = getAttributeValue(attrMap, "type", null);
            if (!("button".equals(type) || "image".equals(type) || "submit"
                .equals(type))) {
                PropertyDesc propertyDesc = processParameterTag(
                    toAnalyzeContext(context), attrs, annotation);
                if ("file".equals(type) && propertyDesc != null) {
                    propertyDesc.getTypeDesc().setClassDesc(
                        FormFile.class.getName());
                }
            }
        } else if ("select".equals(name) || "textarea".equals(name)) {
            processParameterTag(toAnalyzeContext(context), attrs, annotation);
        } else {
            registerClassDesc(analyzeContext, attrMap, "href", "GET");
            registerClassDesc(analyzeContext, attrMap, "src", "GET");
        }

        return super.evaluate(context, name, attrs, body);
    }

    ClassDesc registerClassDesc(AnalyzerContext analyzerContext, Map attrMap,
        String attrName, String method) {

        SourceCreator creator = analyzerContext.getSourceCreator();
        Path path = constructPath(getAttributeValue(attrMap, attrName, null));
        if (path == null) {
            return null;
        }
        String className = creator.getClassName(creator.getComponentName(path
            .getTrunk(), method));
        if (className == null) {
            return null;
        }
        String actionName = creator.getActionName(path.getTrunk(), method);
        if (actionName == null) {
            return null;
        }
        ClassDesc classDesc = analyzerContext.getTemporaryClassDesc(className);
        classDesc.setMethodDesc(new MethodDescImpl(actionName));
        for (Iterator itr = path.getParameterMap().keySet().iterator(); itr
            .hasNext();) {
            classDesc.addProperty((String) itr.next(), PropertyDesc.WRITE
                | PropertyDesc.READ);
        }
        return classDesc;
    }

    Path constructPath(String pathWithParameters) {

        if (pathWithParameters == null) {
            return null;
        } else {
            return new Path(pathWithParameters);
        }
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

    PropertyDesc processParameterTag(AnalyzerContext context,
        Attribute[] attrs, String annotation) {

        String className = context.getFormActionPageClassName();
        if (className == null) {
            return null;
        }

        Map attrMap = evaluate(context, attrs);
        Attribute attr = (Attribute) attrMap.get("name");
        if (attr != null) {
            return context.getPropertyDesc(context
                .getTemporaryClassDesc(className), TagEvaluatorUtils
                .defilter(attr.getValue()), PropertyDesc.READ
                | PropertyDesc.WRITE);
        }
        return null;
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
