package org.seasar.cms.ymir.extension.zpt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.seasar.cms.ymir.FormFile;
import org.seasar.cms.ymir.MatchedPathMapping;
import org.seasar.cms.ymir.Path;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.FormDesc;
import org.seasar.cms.ymir.extension.creator.PropertyDesc;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.impl.FormDescImpl;
import org.seasar.cms.ymir.extension.creator.impl.MethodDescImpl;

import net.skirnir.freyja.Attribute;
import net.skirnir.freyja.Element;
import net.skirnir.freyja.EvaluationRuntimeException;
import net.skirnir.freyja.ExpressionEvaluator;
import net.skirnir.freyja.TagEvaluatorUtils;
import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.TalTagEvaluator;
import net.skirnir.freyja.zpt.ZptTemplateContext;

public class AnalyzerTalTagEvaluator extends TalTagEvaluator {

    public String[] getSpecialTagPatternStrings() {
        return new String[] { "form", "input", "select", "textarea" };
    }

    public TemplateContext newContext() {
        return new AnalyzerContext();
    }

    public String evaluate(TemplateContext context, String name,
            Attribute[] attrs, Element[] body) {

        AnnotationResult result = findAnnotation(context, name, attrs);
        String annotation = result.getAnnotation();
        attrs = result.getTheOtherAttributes();
        AnalyzerContext analyzerContext = toAnalyzerContext(context);
        Map attrMap = evaluate(analyzerContext, attrs);

        if ("form".equals(name)) {
            analyzerContext.setFormDesc(registerTransitionClassDesc(
                    analyzerContext, attrMap, "action", getAttributeValue(
                            attrMap, "method", "GET").toUpperCase()));
            try {
                return super.evaluate(context, name, attrs, body);
            } finally {
                analyzerContext.setFormDesc(null);
            }
        } else if ("input".equals(name)) {
            String type = getAttributeValue(attrMap, "type", null);
            FormDesc formDesc = analyzerContext.getFormDesc();
            if (formDesc != null
                    && formDesc.isDispatchingByRequestParameter()
                    && ("submit".equals(type) || "button".equals(type) || "image"
                            .equals(type))) {
                formDesc.setActionMethodDesc(getAttributeValue(attrMap, "name",
                        null));
            } else {
                PropertyDesc propertyDesc = processParameterTag(
                        analyzerContext, attrMap, annotation);
                if ("file".equals(type) && propertyDesc != null) {
                    propertyDesc.getTypeDesc().setClassDesc(
                            FormFile.class.getName());
                }
            }
        } else if ("select".equals(name) || "textarea".equals(name)) {
            processParameterTag(analyzerContext, attrMap, annotation);
        } else {
            registerTransitionClassDesc(analyzerContext, attrMap, "href", "GET");
            registerTransitionClassDesc(analyzerContext, attrMap, "src", "GET");
        }

        return super.evaluate(context, name, attrs, body);
    }

    FormDesc registerTransitionClassDesc(AnalyzerContext analyzerContext,
            Map attrMap, String attrName, String method) {

        SourceCreator creator = analyzerContext.getSourceCreator();
        Path path = constructPath(analyzerContext.getPathNormalizer()
                .normalize(getAttributeValue(attrMap, attrName, null)));
        if (path == null) {
            return null;
        }
        MatchedPathMapping matched = creator.findMatchedPathMapping(path
                .getTrunk(), method);
        if (matched == null) {
            return null;
        }
        String className = creator.getClassName(matched.getComponentName());
        if (className == null) {
            return null;
        }
        String actionName = matched.getActionName();
        ClassDesc classDesc = analyzerContext.getTemporaryClassDesc(className);
        boolean dispatchingByRequestParameter = matched
                .isDispatchingByRequestParameter();
        classDesc.setMethodDesc(new MethodDescImpl(actionName));
        for (Iterator itr = path.getParameterMap().keySet().iterator(); itr
                .hasNext();) {
            classDesc.addProperty((String) itr.next(), PropertyDesc.WRITE
                    | PropertyDesc.READ);
        }
        return new FormDescImpl(classDesc, actionName,
                dispatchingByRequestParameter);
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
        List<Attribute> attrList = new ArrayList<Attribute>();
        for (int i = 0; i < attrs.length; i++) {
            if ("tal:annotation".equals(attrs[i].getName())) {
                if (annotation != null) {
                    if (!"ignore".equals(behaviorDuplicateTag)) {
                        throw new EvaluationRuntimeException(
                                "Duplicate tag found: "
                                        + attrs[i].getName()
                                        + ": "
                                        + TagEvaluatorUtils.getBeginTagString(
                                                name, attrs)).setLineNumber(
                                attrs[i].getLineNumber()).setColumnNumber(
                                attrs[i].getColumnNumber());
                    }
                }
                annotation = attrs[i].getValue();
            } else {
                attrList.add(attrs[i]);
            }
        }
        return new AnnotationResult(annotation, attrList
                .toArray(new Attribute[0]));
    }

    PropertyDesc processParameterTag(AnalyzerContext context, Map attrMap,
            String annotation) {

        FormDesc formDesc = context.getFormDesc();
        if (formDesc == null) {
            return null;
        }

        String name = getAttributeValue(attrMap, "name", null);
        if (name != null && isValidAsPropertyName(name)) {
            return context.getPropertyDesc(formDesc.getClassDesc(), name,
                    PropertyDesc.READ | PropertyDesc.WRITE);
        }

        return null;
    }

    boolean isValidAsPropertyName(String name) {
        if (name == null) {
            return false;
        }

        int pre = 0;
        int idx;
        while ((idx = name.indexOf('.', pre)) >= 0) {
            if (!isValidAsSinglePropertyName(name.substring(pre, idx))) {
                return false;
            }
            pre = idx + 1;
        }
        if (!isValidAsSinglePropertyName(name.substring(pre))) {
            return false;
        }
        return true;
    }

    boolean isValidAsSinglePropertyName(String name) {
        if (name == null) {
            return false;
        }
        if (name.endsWith("]")) {
            int lbracket = name.indexOf('[');
            if (lbracket < 0) {
                return false;
            }
            name = name.substring(0, lbracket);
        }
        return isValidAsSimplePropertyName(name);
    }

    boolean isValidAsSimplePropertyName(String name) {
        if (name == null || name.length() == 0) {
            return false;
        }
        char ch = name.charAt(0);
        if (!(ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch == '_')) {
            return false;
        }
        for (int i = 1; i < name.length(); i++) {
            ch = name.charAt(i);
            if (!(ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch == '_' || ch >= '0'
                    && ch <= '9')) {
                return false;
            }
        }
        return true;
    }

    Map evaluate(ZptTemplateContext context, Attribute[] attrs) {

        ExpressionEvaluator expEvaluator = context.getExpressionEvaluator();
        VariableResolver varResolver = context.getVariableResolver();

        List<Attribute> attrList = new ArrayList<Attribute>();
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

    AnalyzerContext toAnalyzerContext(TemplateContext context) {

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
