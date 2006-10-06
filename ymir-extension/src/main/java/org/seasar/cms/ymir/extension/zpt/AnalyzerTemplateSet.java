package org.seasar.cms.ymir.extension.zpt;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.Template;

import net.skirnir.freyja.Element;
import net.skirnir.freyja.Macro;
import net.skirnir.freyja.TemplateEvaluator;
import net.skirnir.freyja.TemplateSet;
import net.skirnir.freyja.impl.AbstractPathTemplateSet;

public class AnalyzerTemplateSet implements TemplateSet {

    private TemplateSet templateSet_;

    private TemplateSet localTemplateSet_;

    public AnalyzerTemplateSet(TemplateEvaluator evaluator,
            TemplateSet templateSet, SourceCreator sourceCreator,
            TemplatePathResolver resolver) {

        localTemplateSet_ = new LocalTemplateSet(evaluator, sourceCreator,
                resolver);
        templateSet_ = templateSet;
    }

    public String getCanonicalName(String baseTemplateName, String templateName) {
        if (templateSet_ != null) {
            return templateSet_
                    .getCanonicalName(baseTemplateName, templateName);
        } else {
            return localTemplateSet_.getCanonicalName(baseTemplateName,
                    templateName);
        }
    }

    public Element[] getElements(String templateName, String macroName) {
        Element[] elements = localTemplateSet_.getElements(templateName,
                macroName);
        if (elements != null) {
            return elements;
        } else if (templateSet_ != null) {
            return templateSet_.getElements(templateName, macroName);
        } else {
            return null;
        }
    }

    public Element[] getElements(String templateName) {
        Element[] elements = localTemplateSet_.getElements(templateName);
        if (elements != null) {
            return elements;
        } else if (templateSet_ != null) {
            return templateSet_.getElements(templateName);
        } else {
            return null;
        }
    }

    public Entry getEntry(String templateName) {
        Entry entry = localTemplateSet_.getEntry(templateName);
        if (entry != null) {
            return entry;
        } else if (templateSet_ != null) {
            return templateSet_.getEntry(templateName);
        } else {
            return null;
        }
    }

    public Macro getMacro(Entry entry, String macroName) {
        Macro macro = localTemplateSet_.getMacro(entry, macroName);
        if (macro != null) {
            return macro;
        } else if (templateSet_ != null) {
            return templateSet_.getMacro(entry, macroName);
        } else {
            return null;
        }
    }

    public Macro getMacro(String templateName, String macroName) {
        Macro macro = localTemplateSet_.getMacro(templateName, macroName);
        if (macro != null) {
            return macro;
        } else if (templateSet_ != null) {
            return templateSet_.getMacro(templateName, macroName);
        } else {
            return null;
        }
    }

    public Long getSerialNumber(String templateName) {
        Long serialNumber = localTemplateSet_.getSerialNumber(templateName);
        if (serialNumber != null) {
            return serialNumber;
        } else if (templateSet_ != null) {
            return templateSet_.getSerialNumber(templateName);
        } else {
            return null;
        }
    }

    public boolean hasEntry(String templateName) {
        if (localTemplateSet_.hasEntry(templateName)) {
            return true;
        } else if (templateSet_ != null) {
            return templateSet_.hasEntry(templateName);
        } else {
            return false;
        }
    }

    private class LocalTemplateSet extends AbstractPathTemplateSet {

        private SourceCreator sourceCreator_;

        private TemplatePathResolver resolver_;

        public LocalTemplateSet(TemplateEvaluator evaluator,
                SourceCreator sourceCreator, TemplatePathResolver resolver) {
            super(sourceCreator.getEncoding(), evaluator);
            sourceCreator_ = sourceCreator;
            resolver_ = resolver;
        }

        @Override
        public String getCanonicalName(String baseTemplateName,
                String templateName) {

            return templateSet_
                    .getCanonicalName(baseTemplateName, templateName);
        }

        @Override
        protected InputStream getInputStream(String templateName) {

            Template tempate = getTemplate(templateName);
            if (!tempate.exists()) {
                return null;
            }
            try {
                return tempate.getInputStream();
            } catch (IOException ex) {
                return null;
            }
        }

        @Override
        public Long getSerialNumber(String templateName) {
            return getTemplate(templateName).lastModified();
        }

        public boolean hasEntry(String templateName) {
            return getTemplate(templateName).exists();
        }

        Template getTemplate(String templateName) {
            return sourceCreator_.getTemplateProvider().getTemplate(
                    resolver_.getLocalPath(templateName,
                            getHttpServletRequest()));
        }

        HttpServletRequest getHttpServletRequest() {
            return (HttpServletRequest) sourceCreator_.getApplication()
                    .getS2Container().getComponent(HttpServletRequest.class);
        }
    }
}
