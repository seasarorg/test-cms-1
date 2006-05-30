package org.seasar.cms.framework;

import java.util.Map;

public interface RequestProcessor {

    String DISPATCHER_REQUEST = "REQUEST";

    String DISPATCHER_FORWARD = "FORWARD";

    String DISPATCHER_INCLUDE = "INCLUDE";

    String DISPATCHER_ERROR = "ERROR";

    Response process(String path, String method, String dispatcher,
        Map parameterMap) throws PageNotFoundException;
}
