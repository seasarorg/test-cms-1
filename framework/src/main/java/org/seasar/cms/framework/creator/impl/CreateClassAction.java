package org.seasar.cms.framework.creator.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.MethodDesc;

public class CreateClassAction extends AbstractUpdateAction {

    private static final String PARAM_TRANSITION = SourceCreatorImpl.PARAM_PREFIX
        + "transition";

    public CreateClassAction(SourceCreatorImpl sourceCreator) {
        super(sourceCreator);
    }

    public Response act(Request request, String className, File sourceFile,
        File templateFile) {

        String subTask = request.getParameter(PARAM_SUBTASK);
        if ("create".equals(subTask)) {
            return actCreate(request, className, sourceFile, templateFile);
        } else {
            return actDefault(request, className, sourceFile, templateFile);
        }
    }

    Response actDefault(Request request, String className, File sourceFile,
        File templateFile) {

        String actionName = getSourceCreator().getActionName(request.getPath(),
            request.getMethod());

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("className", className);
        variableMap.put("actionName", actionName);
        return getSourceCreator().getResponseCreator().createResponse(
            "createClass", variableMap);
    }

    Response actCreate(Request request, String className, File sourceFile,
        File templateFile) {

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        String transition = request.getParameter(PARAM_TRANSITION);

        ClassDesc classDesc = new ClassDesc(className);
        MethodDesc methodDesc = new MethodDesc(getSourceCreator()
            .getActionName(request.getPath(), method));
        methodDesc.setReturnType(String.class.getName());
        if (transition != null && transition.trim().length() > 0) {
            methodDesc.setBody("return " + quote(transition.trim()) + ";");
        }
        classDesc.setMethodDesc(methodDesc);

        writeSourceFile(classDesc);

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("method", method);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("className", className);
        return getSourceCreator().getResponseCreator().createResponse(
            "createClass_create", variableMap);
    }
}
