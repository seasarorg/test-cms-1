package org.seasar.cms.ymir.extension.creator;

import java.io.InputStream;
import java.util.Map;

public interface TemplateAnalyzer {

    void analyze(String path, String method,
            Map<String, ClassDesc> classDescriptorMap, InputStream inputStream,
            String encoding, String className);
}
