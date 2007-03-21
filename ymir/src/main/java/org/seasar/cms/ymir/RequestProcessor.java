package org.seasar.cms.ymir;

import java.util.Locale;
import java.util.Map;

public interface RequestProcessor {

    String ACTION_DEFAULT = "_default";

    String ACTION_RENDER = "_render";

    String ACTION_VALIDATIONFAILED = "_validationFailed";

    String ACTION_PERMISSIONDENIED = "_permissionDenied";

    String ATTR_SELF = "self";

    String ATTR_NOTES = "notes";

    Request prepareForProcessing(String contextPath, String path,
            String method, String dispatcher, Map parameterMap,
            Map fileParameterMap, AttributeContainer attributeContainer,
            Locale locale);

    Response process(Request request) throws PageNotFoundException,
            PermissionDeniedException;

    Object backupForInclusion(AttributeContainer attributeContainer);

    void restoreForInclusion(AttributeContainer attributeContainer,
            Object backupped);
}
