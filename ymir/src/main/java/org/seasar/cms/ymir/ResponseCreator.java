package org.seasar.cms.ymir;

import java.net.URL;
import java.util.Map;

public interface ResponseCreator {

    Response createResponse(String templateName, Map variableMap);

    Response createResponse(URL templateURL, Map variableMap);
}
