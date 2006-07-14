package org.seasar.cms.ymir.creator;

import java.io.InputStream;
import java.util.Map;

public interface TemplateAnalyzer {

    void analyze(String method, Map<String, ClassDesc> classDescriptorMap,
        InputStream inputStream, String encoding, String className);
}
