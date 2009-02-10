package org.seasar.cms.mailsender;

public interface TemplateEvaluator<C> {

    C getDefaultConfiguration();

    Class<? extends C> getConfigurationType();

    String evaluateTemple(C configuration, String path, Object root);
}
