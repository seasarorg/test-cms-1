package org.seasar.cms.ymir;

import java.util.regex.Pattern;

import org.seasar.kvasir.util.el.VariableResolver;

public interface PathMapping {

    String getActionNameTemplate();

    String getComponentNameTemplate();

    String getPathInfoTemplate();

    String getDefaultReturnValueTemplate();

    Object getDefaultReturnValue();

    Pattern getPattern();

    VariableResolver match(String path, String method);

    String getComponentName(VariableResolver resolver);

    String getActionName(VariableResolver resolver);

    String getPathInfo(VariableResolver resolver);

    Object getDefaultReturnValue(VariableResolver variableResolver_);

    boolean isDenied();

    String extractParameterName(String name);

    boolean isDispatchingByParameter();
}