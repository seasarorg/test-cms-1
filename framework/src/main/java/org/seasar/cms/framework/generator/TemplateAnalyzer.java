package org.seasar.cms.framework.generator;

import java.io.InputStream;
import java.util.Map;

public interface TemplateAnalyzer {

    void analyze(Map classDescriptorMap, InputStream inputStream,
        String encoding, String className);
}
