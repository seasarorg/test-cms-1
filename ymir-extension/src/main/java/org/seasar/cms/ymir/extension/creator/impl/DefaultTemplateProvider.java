package org.seasar.cms.ymir.extension.creator.impl;

import java.io.File;

import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.Template;
import org.seasar.cms.ymir.extension.creator.TemplateProvider;

public class DefaultTemplateProvider implements TemplateProvider {

    private SourceCreator sourceCreator_;

    public DefaultTemplateProvider(SourceCreator sourceCreator) {
        sourceCreator_ = sourceCreator;
    }

    public Template getTemplate(String path) {
        return new FileTemplate(path, new File(sourceCreator_
                .getWebappSourceRoot(), path));
    }
}
