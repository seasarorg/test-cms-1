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

    private String defaultPathTemplate_;

    private boolean denied_;

    public PathMappingImpl() {
    }

    public PathMappingImpl(String patternString, String componentTemplate,
        String actionNameTemplate, String pathInfoTemplate,
        String defaultPathTemplate) {

        this(false, patternString, componentTemplate, actionNameTemplate,
            pathInfoTemplate, defaultPathTemplate);
    }

    public PathMappingImpl(boolean denied, String patternString,
        String componentTemplate, String actionNameTemplate,
        String pathInfoTemplate, String defaultPathTemplate) {

        pattern_ = Pattern.compile(patternString);
        componentNameTemplate_ = componentTemplate;
        actionNameTemplate_ = actionNameTemplate;
        pathInfoTemplate_ = pathInfoTemplate;
        defaultPathTemplate_ = defaultPathTemplate;
    }

    public String getActionNameTemplate() {

        return actionNameTemplate_;
    }

    public void setActionNameTemplate(String actionNameTemplate) {

        actionNameTemplate_ = actionNameTemplate;
    }

    public String getComponentNameTemplate() {
        return componentNameTemplate_;
    }

    public void setComponentNameTemplate(String componentTemplate) {

        componentNameTemplate_ = componentTemplate;
    }

    public String getPathInfoTemplate() {

        return pathInfoTemplate_;
    }

    public void setPathInfoTemplate(String pathInfoTemplate) {

        pathInfoTemplate_ = pathInfoTemplate;
    }

    public String getDefaultPathTemplate() {

        return defaultPathTemplate_;
    }

    public void setDefaultPathTemplate(String defaultPathTemplate) {

        defaultPathTemplate_ = defaultPathTemplate;
    }

    public Pattern getPattern() {

        return pattern_;
    }

    public void setPattern(Pattern pattern) {

        pattern_ = pattern;
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
            prop.put("method", method.toLowerCase());
            prop.put("Method", upper(method));

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

    public String getDefaultPath(VariableResolver resolver) {

        return evaluate(defaultPathTemplate_, resolver);
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
}
