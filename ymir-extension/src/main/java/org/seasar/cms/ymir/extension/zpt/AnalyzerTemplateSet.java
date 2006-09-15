package org.seasar.cms.ymir.extension.zpt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.seasar.cms.ymir.extension.creator.SourceCreator;

import net.skirnir.freyja.TemplateEvaluator;
import net.skirnir.freyja.impl.AbstractPathTemplateSet;

public class AnalyzerTemplateSet extends AbstractPathTemplateSet {

    private SourceCreator sourceCreator_;

    private TemplatePathNormalizer pathNormalizer_;

    public AnalyzerTemplateSet(String pageEncoding,
            TemplateEvaluator evaluator, SourceCreator sourceCreator) {
        super(pageEncoding, evaluator);
        sourceCreator_ = sourceCreator;
    }

    @Override
    public String getCanonicalName(String baseTemplateName, String templateName) {

        return super.getCanonicalName(baseTemplateName, pathNormalizer_
                .normalize(templateName));
    }

    @Override
    protected InputStream getInputStream(String templateName) {
        try {
            return new FileInputStream(getFile(templateName));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("Can't get input stream of template: "
                    + templateName, ex);
        }
    }

    @Override
    public Long getSerialNumber(String templateName) {
        return getFile(templateName).lastModified();
    }

    public boolean hasEntry(String templateName) {
        return getFile(templateName).exists();
    }

    File getFile(String templateName) {
        return new File(sourceCreator_.getWebappSourceRoot(), templateName);
    }

    public void setPathNormalizer(TemplatePathNormalizer pathNormalizer) {
        pathNormalizer_ = pathNormalizer;
    }
}
