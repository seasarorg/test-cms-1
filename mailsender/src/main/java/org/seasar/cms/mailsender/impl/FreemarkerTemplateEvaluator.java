package org.seasar.cms.mailsender.impl;

import java.io.StringReader;
import java.io.StringWriter;

import org.seasar.cms.mailsender.TemplateEvaluator;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.seasar.framework.container.annotation.tiger.BindingType;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class FreemarkerTemplateEvaluator implements
        TemplateEvaluator<Configuration> {
    private Configuration configuration;

    @Binding(bindingType = BindingType.MAY)
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public String evaluateResourceTemplate(Configuration configuration,
            String path, Object root) {
        StringWriter sw = new StringWriter();
        try {
            configuration.getTemplate(path).process(root, sw);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return sw.toString();
    }

    public String evaluateStringTemplate(Configuration configuration,
            String template, Object root) {
        StringWriter sw = new StringWriter();
        try {
            new Template("template", new StringReader(template), configuration)
                    .process(root, sw);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return sw.toString();
    }

    public Class<? extends Configuration> getConfigurationType() {
        return Configuration.class;
    }

    public Configuration getDefaultConfiguration() {
        if (configuration != null) {
            return configuration;
        } else {
            return new Configuration();
        }
    }
}
