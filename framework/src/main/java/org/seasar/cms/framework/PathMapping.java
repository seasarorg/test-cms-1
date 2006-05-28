package org.seasar.cms.framework;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.seasar.kvasir.util.el.EvaluationException;
import org.seasar.kvasir.util.el.TextTemplateEvaluator;
import org.seasar.kvasir.util.el.VariableResolver;
import org.seasar.kvasir.util.el.impl.MapVariableResolver;
import org.seasar.kvasir.util.el.impl.SimpleTextTemplateEvaluator;

public class PathMapping {

    private TextTemplateEvaluator evaluator_ = new SimpleTextTemplateEvaluator();

    private Pattern pattern_;

    private String componentNameTemplate_;

    private String actionNameTemplate_;

    private String pathInfoTemplate_;

    public PathMapping() {
    }

    public PathMapping(String patternString, String componentTemplate,
        String actionNameTemplate, String pathInfoTemplate) {

        pattern_ = Pattern.compile(patternString);
        componentNameTemplate_ = componentTemplate;
        actionNameTemplate_ = actionNameTemplate;
        pathInfoTemplate_ = pathInfoTemplate;
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

    public Pattern getPattern() {

        return pattern_;
    }

    public void setPattern(Pattern pattern) {

        pattern_ = pattern;
    }

    public VariableResolver match(String path) {

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

        if (resolver == null) {
            return null;
        } else {
            try {
                return evaluator_.evaluateAsString(componentNameTemplate_,
                    resolver);
            } catch (EvaluationException ex) {
                throw new RuntimeException("Can't evaluate template: "
                    + componentNameTemplate_ + ", resolver=" + resolver, ex);
            }
        }
    }

    public String getActionName(VariableResolver resolver) {

        if (resolver == null) {
            return null;
        } else {
            try {
                return evaluator_.evaluateAsString(actionNameTemplate_,
                    resolver);
            } catch (EvaluationException ex) {
                throw new RuntimeException("Can't evaluate template: "
                    + actionNameTemplate_ + ", resolver=" + resolver, ex);
            }
        }
    }

    public String getPathInfo(VariableResolver resolver) {

        if (resolver == null) {
            return null;
        } else {
            try {
                return evaluator_.evaluateAsString(pathInfoTemplate_, resolver);
            } catch (EvaluationException ex) {
                throw new RuntimeException("Can't evaluate template: "
                    + pathInfoTemplate_ + ", resolver=" + resolver, ex);
            }

        }
    }
}
