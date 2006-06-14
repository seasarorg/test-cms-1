package org.seasar.cms.framework.creator;


public interface SourceGenerator {

    String generatePageSource(ClassDesc classDesc);

    String generatePageBaseSource(ClassDesc classDesc);
}
