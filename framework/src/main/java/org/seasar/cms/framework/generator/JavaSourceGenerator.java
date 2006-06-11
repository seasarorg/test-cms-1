package org.seasar.cms.framework.generator;

public interface JavaSourceGenerator {

    String generatePageSource(ClassDesc classDesc);

    String generatePageBaseSource(ClassDesc classDesc);
}
