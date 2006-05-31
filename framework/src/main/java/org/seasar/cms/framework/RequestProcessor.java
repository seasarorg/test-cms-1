package org.seasar.cms.framework;

import java.util.Map;

public interface RequestProcessor {

    Response process(String path, String method, String dispatcher,
        Map parameterMap) throws PageNotFoundException;
}
