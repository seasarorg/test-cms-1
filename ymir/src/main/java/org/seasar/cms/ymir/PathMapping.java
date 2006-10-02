package org.seasar.cms.ymir;

import java.util.regex.Pattern;

import org.seasar.kvasir.util.el.VariableResolver;

public interface PathMapping {

    String getActionNameTemplate();

    void setActionNameTemplate(String actionNameTemplate);

    String getComponentNameTemplate();

    void setComponentNameTemplate(String componentTemplate);

    String getPathInfoTemplate();

    void setPathInfoTemplate(String pathInfoTemplate);

    String getDefaultReturnValueTemplate();

    void setDefaultReturnValueTemplate(String defaultReturnValueTemplate);

    Object getDefaultReturnValue();

    void setDefaultReturnValue(Object defaultReturnValue);

    Pattern getPattern();

    void setPattern(Pattern pattern);

    VariableResolver match(String path, String method);

    String getComponentName(VariableResolver resolver);

    String getActionName(VariableResolver resolver);

    String getPathInfo(VariableResolver resolver);

    Object getDefaultReturnValue(VariableResolver variableResolver_);

    boolean isDenied();

    void setDenied(boolean denied);

    boolean isDispatchingByRequestParameter();

    void setDispatchingByRequestParameter(boolean dispatchingByRequestParameter);
}