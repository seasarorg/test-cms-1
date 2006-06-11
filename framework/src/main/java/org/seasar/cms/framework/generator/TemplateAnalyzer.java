package org.seasar.cms.framework.generator;

import java.io.InputStream;
import java.util.Map;

public interface TemplateAnalyzer {

    void analyze(PageClassGenerator generator,
        Map classDescriptorMap, InputStream inputStream, String encoding,
        String className);
}
