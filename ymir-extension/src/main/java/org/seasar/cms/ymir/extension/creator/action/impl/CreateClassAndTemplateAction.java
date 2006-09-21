package org.seasar.cms.ymir.extension.creator.action.impl;

import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.MethodDesc;
import org.seasar.cms.ymir.extension.creator.PathMetaData;
import org.seasar.cms.ymir.extension.creator.impl.BodyDescImpl;
import org.seasar.cms.ymir.extension.creator.impl.MethodDescImpl;
import org.seasar.cms.ymir.extension.creator.impl.SourceCreatorImpl;
import org.seasar.cms.ymir.impl.DefaultRequestProcessor;

public class CreateClassAndTemplateAction extends AbstractUpdateAction {

    private static final String PARAM_REDIRECTPATH = SourceCreatorImpl.PARAM_PREFIX
            + "redirectPath";

    public CreateClassAndTemplateAction(SourceCreatorImpl sourceCreator) {
        super(sourceCreator);
    }

    public Response act(Request request, PathMetaData pathMetaData) {

        String subTask = request.getParameter(PARAM_SUBTASK);
        if ("template".equals(subTask)) {
            return actTemplate(request, pathMetaData);
        } else if ("redirect".equals(subTask)) {
            return actRedirect(request, pathMetaData);
        } else {
            return actDefault(request, pathMetaData);
        }
    }

    Response actDefault(Request request, PathMetaData pathMetaData) {

        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("request", request);
        variableMap.put("pathMetaData", pathMetaData);
        return getSourceCreator().getResponseCreator().createResponse(
                "createClassAndTemplate", variableMap);
    }

    Response actTemplate(Request request, PathMetaData pathMetaData) {

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        String template = getSourceCreator().getSourceGenerator()
                .generateTemplateSource(
                        getSuffix(pathMetaData.getTemplateFile().getName()),
                        new HashMap());
        if (template == null) {
            template = "";
        }

        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("request", request);
        variableMap.put("method", method);
        variableMap.put("pathMetaData", pathMetaData);
        variableMap.put("template", template);
        return getSourceCreator().getResponseCreator().createResponse(
                "createClassAndTemplate_template", variableMap);
    }

    Response actRedirect(Request request, PathMetaData pathMetaData) {

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        String redirectPath = request.getParameter(PARAM_REDIRECTPATH);
        if (redirectPath == null) {
            return null;
        }

        ClassDesc classDesc = getSourceCreator().newClassDesc(
                pathMetaData.getClassName());
        MethodDesc methodDesc = new MethodDescImpl(getSourceCreator()
                .getActionName(request.getPath(), method));
        methodDesc.setReturnTypeDesc(String.class.getName());
        methodDesc.setBodyDesc(new BodyDescImpl("return "
                + quote("redirect:" + redirectPath) + ";"));
        classDesc.setMethodDesc(methodDesc);
        classDesc.setMethodDesc(new MethodDescImpl(
                DefaultRequestProcessor.ACTION_RENDER));

        getSourceCreator().writeSourceFile(classDesc, null);

        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("request", request);
        variableMap.put("method", method);
        variableMap.put("pathMetaData", pathMetaData);
        return getSourceCreator().getResponseCreator().createResponse(
                "createClassAndTemplate_redirect", variableMap);
    }
}
