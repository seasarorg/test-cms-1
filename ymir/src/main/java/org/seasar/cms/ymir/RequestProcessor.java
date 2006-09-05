package org.seasar.cms.ymir;

import java.util.Map;

public interface RequestProcessor {

    Response process(String contextPath, String path, String method,
            String dispatcher, Map parameterMap, Map fileParameterMap)
            throws PageNotFoundException;

    Object backupForInclusion();

    void restoreForInclusion(Object backupped);
}
