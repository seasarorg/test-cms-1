package org.seasar.cms.ymir.freemarker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.seasar.cms.ymir.creator.BodyDesc;
import org.seasar.cms.ymir.creator.ClassDesc;
import org.seasar.cms.ymir.creator.EntityMetaData;
import org.seasar.cms.ymir.creator.MethodDesc;
import org.seasar.cms.ymir.creator.SourceCreator;
import org.seasar.cms.ymir.creator.SourceGenerator;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateException;

public class FreemarkerSourceGenerator implements SourceGenerator {

    private SourceCreator sourceCreator_;

    public String generateGapSource(ClassDesc classDesc) {

        if (classDesc == null) {
            return null;
        }
        return generateClassSource(classDesc.getKind() + ".java", classDesc);
    }

    public String generateBaseSource(ClassDesc classDesc) {

        if (classDesc == null) {
            return null;
        }
        return generateClassSource(classDesc.getKind() + "Base.java", classDesc);
    }

    String generateClassSource(String templateName, ClassDesc classDesc) {

        MethodDesc[] mds = classDesc.getMethodDescs();
        for (int i = 0; i < mds.length; i++) {
            mds[i].setEvaluatedBody(generateSource(mds[i].getBodyDesc()));
        }
        Map<String, Object> root = new HashMap<String, Object>();
        root.put("classDesc", classDesc);
        root.put("entityMetaData", new EntityMetaData(sourceCreator_, classDesc
            .getName()));

        return generateSource(templateName, root);
    }

    public String generateTemplateSource(String suffix, Object root) {

        return generateSource("Template" + suffix, root);
    }

    public String generateSource(BodyDesc bodyDesc) {

        if (bodyDesc == null) {
            return null;
        }
        return generateSource("Body." + bodyDesc.getKey(), bodyDesc.getRoot());
    }

    String generateSource(String templateName, Object root) {

        Configuration cfg = new Configuration();
        cfg.setEncoding(Locale.getDefault(), "UTF-8");
        cfg.setTemplateLoader(new ClassTemplateLoader(getClass(), "template"));
        cfg.setObjectWrapper(new DefaultObjectWrapper());

        StringWriter sw = new StringWriter();
        try {
            cfg.getTemplate(templateName + ".ftl").process(root, sw);
        } catch (TemplateException ex) {
            throw new RuntimeException(ex);
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return sw.toString();
    }

    public void setSourceCreator(SourceCreator sourceCreator) {

        sourceCreator_ = sourceCreator;
    }
}
