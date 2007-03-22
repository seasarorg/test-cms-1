package org.seasar.cms.ymir.extension.creator.action.impl;

import static org.seasar.cms.ymir.impl.DefaultRequestProcessor.PARAM_METHOD;

import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.InvalidClassDescException;
import org.seasar.cms.ymir.extension.creator.MethodDesc;
import org.seasar.cms.ymir.extension.creator.PathMetaData;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.action.UpdateAction;
import org.seasar.cms.ymir.extension.creator.impl.BodyDescImpl;
import org.seasar.cms.ymir.extension.creator.impl.MethodDescImpl;

public class CreateClassAction extends AbstractAction implements UpdateAction {

    private static final String PARAM_TRANSITION = SourceCreator.PARAM_PREFIX
            + "transition";

    private static final String PARAM_TRANSITIONREDIRECT = PARAM_TRANSITION
            + "_redirect";

    public CreateClassAction(SourceCreator sourceCreator) {
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

        Map<String, Object> variableMap = new HashMap<String, Object>();
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
        boolean redirect = "true".equals(request
                .getParameter(PARAM_TRANSITIONREDIRECT));

        ClassDesc classDesc = getSourceCreator().newClassDesc(
                pathMetaData.getClassName());
        MethodDesc methodDesc = new MethodDescImpl(getSourceCreator()
                .getActionName(request.getPath(), method));
        methodDesc.setReturnTypeDesc(String.class.getName(), true);
        if (transition != null && transition.trim().length() > 0) {
            if (redirect) {
                transition = "redirect:" + transition;
            }
            methodDesc.setBodyDesc(new BodyDescImpl("return "
                    + quote(transition.trim()) + ";"));
        }
        classDesc.setMethodDesc(methodDesc);

        String[] lackingClassNames = null;
        try {
            getSourceCreator().writeSourceFile(classDesc, null);
        } catch (InvalidClassDescException ex) {
            lackingClassNames = ex.getLackingClassNames();
        }

        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("request", request);
        variableMap.put("method", method);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("pathMetaData", pathMetaData);
        variableMap.put("lackingClassNames", lackingClassNames);
        return getSourceCreator().getResponseCreator().createResponse(
                "createClass_create", variableMap);
    }
}
