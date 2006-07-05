package org.seasar.cms.framework.creator.action.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.creator.impl.SourceCreatorImpl;

public class SystemConsoleAction extends AbstractUpdateAction {

    public SystemConsoleAction(SourceCreatorImpl sourceCreator) {
        super(sourceCreator);
    }

    public Response act(Request request, String className, File sourceFile,
        File templateFile) {

        String subTask = request.getParameter(PARAM_SUBTASK);
        if ("updateAllClasses".equals(subTask)) {
            return actUpdateAllClasses(request, className, sourceFile,
                templateFile);
        } else {
            return actDefault(request, className, sourceFile, templateFile);
        }
    }

    Response actUpdateAllClasses(Request request, String className,
        File sourceFile, File templateFile) {

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("method", method);
        return getSourceCreator().getResponseCreator().createResponse(
            "systemConsole_updateAllClasses", variableMap);
    }

    Response actDefault(Request request, String className, File sourceFile,
        File templateFile) {

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("method", request.getMethod());
        return getSourceCreator().getResponseCreator().createResponse(
            "systemConsole", variableMap);
    }
}
