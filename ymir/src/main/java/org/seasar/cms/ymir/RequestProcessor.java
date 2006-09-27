package org.seasar.cms.ymir;

import java.util.Map;

public interface RequestProcessor {

    Request prepareForProcessing(String contextPath, String path,
            String method, String dispatcher, Map parameterMap,
            Map fileParameterMap, AttributeContainer attributeContainer)
            throws PageNotFoundException;

    Response process(Request request);

    Object backupForInclusion(AttributeContainer attributeContainer);

    void restoreForInclusion(AttributeContainer attributeContainer,
            Object backupped);
}
