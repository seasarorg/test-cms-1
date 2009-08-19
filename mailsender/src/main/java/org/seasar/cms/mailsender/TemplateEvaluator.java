package org.seasar.cms.mailsender;

public interface TemplateEvaluator<C> {
    C getDefaultConfiguration();

    Class<? extends C> getConfigurationType();

    String evaluateResourceTemplate(C configuration, String path, Object root);

    String evaluateStringTemplate(C configuration, String template, Object root);
}
