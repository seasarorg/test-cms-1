package org.seasar.cms.ymir;

import java.util.Locale;
import java.util.Map;

public interface RequestProcessor {

    Request prepareForProcessing(String contextPath, String path,
            String method, String dispatcher, Map parameterMap,
            Map fileParameterMap, AttributeContainer attributeContainer,
            Locale locale) throws PageNotFoundException;

    Response process(Request request) throws PermissionDeniedException;

    Object backupForInclusion(AttributeContainer attributeContainer);

    void restoreForInclusion(AttributeContainer attributeContainer,
            Object backupped);
}
