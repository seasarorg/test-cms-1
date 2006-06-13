package org.seasar.cms.framework.generator;

public interface SourceGenerator {

    String generatePageSource(ClassDesc classDesc);

    String generatePageBaseSource(ClassDesc classDesc);
}
