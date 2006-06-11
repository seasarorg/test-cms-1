package org.seasar.cms.framework;

import java.util.Map;

public interface ResponseCreator {

    Response createResponse(String string, Map variableMap);
}
