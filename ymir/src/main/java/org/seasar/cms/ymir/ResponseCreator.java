package org.seasar.cms.ymir;

import java.util.Map;

public interface ResponseCreator {

    Response createResponse(String string, Map variableMap);
}
