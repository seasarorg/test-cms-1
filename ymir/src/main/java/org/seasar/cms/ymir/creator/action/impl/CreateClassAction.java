package org.seasar.cms.ymir.creator.action.impl;

import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.creator.ClassDesc;
import org.seasar.cms.ymir.creator.MethodDesc;
import org.seasar.cms.ymir.creator.PathMetaData;
import org.seasar.cms.ymir.creator.impl.BodyDescImpl;
import org.seasar.cms.ymir.creator.impl.ClassDescImpl;
import org.seasar.cms.ymir.creator.impl.MethodDescImpl;
import org.seasar.cms.ymir.creator.impl.SourceCreatorImpl;

public class CreateClassAction extends AbstractUpdateAction {

    private static final String PARAM_TRANSITION = SourceCreatorImpl.PARAM_PREFIX
        + "transition";

    public CreateClassAction(SourceCreatorImpl sourceCreator) {
        super(sourceCreator);
    }

    public Response act(Request request, PathMetaData pathMetaData) {

        String subTask = request.getParameter(PARAM_SUBTASK);
        if ("create".equals(subTask)) {
            return actCreate(request, pathMetaData);
        } else {
            return actDefault(request, pathMetaData);
        }
    }

    Response actDefault(Request request, PathMetaData pathMetaData) {

        String actionName = getSourceCreator().getActionName(request.getPath(),
            request.getMethod());

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("pathMetaData", pathMetaData);
        variableMap.put("actionName", actionName);
        return getSourceCreator().getResponseCreator().createResponse(
            "createClass", variableMap);
    }

    Response actCreate(Request request, PathMetaData pathMetaData) {

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        String transition = request.getParameter(PARAM_TRANSITION);

        ClassDesc classDesc = new ClassDescImpl(pathMetaData.getClassName());
        MethodDesc methodDesc = new MethodDescImpl(getSourceCreator()
            .getActionName(request.getPath(), method));
        methodDesc.setReturnTypeDesc(String.class.getName());
        if (transition != null && transition.trim().length() > 0) {
            methodDesc.setBodyDesc(new BodyDescImpl("return "
                + quote(transition.trim()) + ";"));
        }
        classDesc.setMethodDesc(methodDesc);

        getSourceCreator().writeSourceFile(classDesc, null);

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("method", method);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("pathMetaData", pathMetaData);
        return getSourceCreator().getResponseCreator().createResponse(
            "createClass_create", variableMap);
    }
}
