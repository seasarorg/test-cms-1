package org.seasar.cms.framework;

import java.util.Map;

public interface RequestProcessor {

    Response process(String contextPath, String path, String method,
        String dispatcher, Map parameterMap) throws PageNotFoundException;
}
