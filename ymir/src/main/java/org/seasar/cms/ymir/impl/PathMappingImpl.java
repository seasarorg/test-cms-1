package org.seasar.cms.ymir.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.seasar.cms.ymir.PathMapping;
import org.seasar.kvasir.util.el.EvaluationException;
import org.seasar.kvasir.util.el.TextTemplateEvaluator;
import org.seasar.kvasir.util.el.VariableResolver;
import org.seasar.kvasir.util.el.impl.MapVariableResolver;
import org.seasar.kvasir.util.el.impl.SimpleTextTemplateEvaluator;

public class PathMappingImpl implements PathMapping {

    private TextTemplateEvaluator evaluator_ = new SimpleTextTemplateEvaluator();

    private Pattern pattern_;

    private String componentNameTemplate_;

    private String actionNameTemplate_;

    private String pathInfoTemplate_;

    private String defaultReturnValueTemplate_;

    private Object defaultReturnValue_;

    private Pattern parameterNamePatternForDispatching_;

    private String parameterNamePatternStringForDispatching_;

    private boolean denied_;

    public PathMappingImpl(String patternString, String componentTemplate,
            String actionNameTemplate, String pathInfoTemplate,
            Object defaultReturnValue,
            String parameterNamePatternStringForDispatching) {

        this(false, patternString, componentTemplate, actionNameTemplate,
                pathInfoTemplate, defaultReturnValue,
                parameterNamePatternStringForDispatching);
    }

    public PathMappingImpl(boolean denied, String patternString,
            String componentTemplate, String actionNameTemplate,
            String pathInfoTemplate, Object defaultReturnValue,
            String parameterNamePatternStringForDispatching) {

        pattern_ = Pattern.compile(patternString);
        componentNameTemplate_ = componentTemplate;
        actionNameTemplate_ = actionNameTemplate;
        pathInfoTemplate_ = pathInfoTemplate;
        if (defaultReturnValue instanceof String) {
            defaultReturnValueTemplate_ = (String) defaultReturnValue;
        } else {
            defaultReturnValue_ = defaultReturnValue;
        }
        if (parameterNamePatternStringForDispatching != null) {
            parameterNamePatternStringForDispatching_ = parameterNamePatternStringForDispatching;
            parameterNamePatternForDispatching_ = Pattern
                    .compile(parameterNamePatternStringForDispatching);
        }
    }

    public String getActionNameTemplate() {

        return actionNameTemplate_;
    }

    public String getComponentNameTemplate() {
        return componentNameTemplate_;
    }

    public String getPathInfoTemplate() {

        return pathInfoTemplate_;
    }

    public String getDefaultReturnValueTemplate() {

        return defaultReturnValueTemplate_;
    }

    public Object getDefaultReturnValue() {

        return defaultReturnValue_;
    }

    public Pattern getPattern() {

        return pattern_;
    }

    public VariableResolver match(String path, String method) {

        Matcher matcher = pattern_.matcher(path);
        if (matcher.find()) {
            Map prop = new HashMap();
            int count = matcher.groupCount();
            for (int j = 0; j <= count; j++) {
                String matched = matcher.group(j);
                prop.put(String.valueOf(j), matched);
                prop.put(j + "u", upper(matched));
                prop.put(j + "l", lower(matched));
            }
            prop.put("`", path.substring(0, matcher.start()));
            prop.put("&", path.substring(matcher.start(), matcher.end()));
            prop.put("'", path.substring(matcher.end()));
            prop.put("METHOD", method);
            String lmethod = method.toLowerCase();
            prop.put("method", lmethod);
            prop.put("Method", upper(lmethod));

            return new MapVariableResolver(prop);
        } else {
            return null;
        }
    }

    String upper(String str) {

        if (str == null) {
            return null;
        } else if (str.length() == 0) {
            return str;
        } else {
            return Character.toUpperCase(str.charAt(0)) + str.substring(1);
        }
    }

    String lower(String str) {

        if (str == null) {
            return null;
        } else if (str.length() == 0) {
            return str;
        } else {
            return Character.toLowerCase(str.charAt(0)) + str.substring(1);
        }
    }

    public String getComponentName(VariableResolver resolver) {

        return evaluate(componentNameTemplate_, resolver);
    }

    public String getActionName(VariableResolver resolver) {

        return evaluate(actionNameTemplate_, resolver);
    }

    public String getPathInfo(VariableResolver resolver) {

        return evaluate(pathInfoTemplate_, resolver);
    }

    public Object getDefaultReturnValue(VariableResolver resolver) {

        if (defaultReturnValueTemplate_ != null) {
            return evaluate(defaultReturnValueTemplate_, resolver);
        } else {
            return defaultReturnValue_;
        }
    }

    String evaluate(String template, VariableResolver resolver) {

        if (resolver == null || template == null) {
            return null;
        } else {
            try {
                return evaluator_.evaluateAsString(template, resolver);
            } catch (EvaluationException ex) {
                throw new RuntimeException("Can't evaluate template: "
                        + template + ", resolver=" + resolver, ex);
            }
        }
    }

    public boolean isDenied() {

        return denied_;
    }

    public String extractParameterName(String name) {

        if (parameterNamePatternForDispatching_ != null) {
            Matcher matcher = parameterNamePatternForDispatching_.matcher(name);
            if (matcher.find()) {
                if (matcher.groupCount() > 0) {
                    return matcher.group(1);
                } else {
                    // 「()」が指定されていない。
                    throw new IllegalArgumentException(
                            "parameter pattern must have ( ) specification: "
                                    + parameterNamePatternStringForDispatching_);
                }
            }
        }
        return null;
    }

    public boolean isDispatchingByParameter() {

        return (parameterNamePatternForDispatching_ != null);
    }
}
