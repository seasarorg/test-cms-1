package org.seasar.cms.framework.creator;

public interface SourceGenerator {

    String generateGapSource(ClassDesc classDesc);

    String generateBaseSource(ClassDesc classDesc);

    String generateTemplateSource(String suffix, Object root);

    String generateSource(BodyDesc bodyDesc);
}
