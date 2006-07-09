package org.seasar.cms.framework;

import java.util.regex.Pattern;

import org.seasar.kvasir.util.el.VariableResolver;

public interface PathMapping {

    String getActionNameTemplate();

    void setActionNameTemplate(String actionNameTemplate);

    String getComponentNameTemplate();

    void setComponentNameTemplate(String componentTemplate);

    String getPathInfoTemplate();

    void setPathInfoTemplate(String pathInfoTemplate);

    String getDefaultPathTemplate();

    void setDefaultPathTemplate(String defaultPathTemplate);

    Pattern getPattern();

    void setPattern(Pattern pattern);

    VariableResolver match(String path, String method);

    String getComponentName(VariableResolver resolver);

    String getActionName(VariableResolver resolver);

    String getPathInfo(VariableResolver resolver);

    String getDefaultPath(VariableResolver resolver);

    boolean isDenied();
}