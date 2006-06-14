package org.seasar.cms.framework.freemarker;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.SourceGenerator;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateException;

public class FreemarkerSourceGenerator implements SourceGenerator {

    public String generateGapSource(ClassDesc classDesc) {

        return generateSource(classDesc.getKind(), classDesc);
    }

    public String generateBaseSource(ClassDesc classDesc) {

        return generateSource(classDesc.getKind() + "Base", classDesc);
    }

    String generateSource(String templateName, Object root) {

        Configuration cfg = new Configuration();
        cfg.setEncoding(Locale.getDefault(), "UTF-8");
        cfg.setTemplateLoader(new ClassTemplateLoader(getClass(), "template"));
        cfg.setObjectWrapper(new DefaultObjectWrapper());

        StringWriter sw = new StringWriter();
        try {
            cfg.getTemplate(templateName + ".java.ftl").process(root, sw);
        } catch (TemplateException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return sw.toString();
    }
}
