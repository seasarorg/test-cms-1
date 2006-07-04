package org.seasar.cms.framework.creator.action.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.MethodDesc;
import org.seasar.cms.framework.creator.impl.BodyDescImpl;
import org.seasar.cms.framework.creator.impl.ClassDescImpl;
import org.seasar.cms.framework.creator.impl.MethodDescImpl;
import org.seasar.cms.framework.creator.impl.SimpleClassDesc;
import org.seasar.cms.framework.creator.impl.SourceCreatorImpl;
import org.seasar.cms.framework.impl.DefaultRequestProcessor;

public class CreateClassAndTemplateAction extends AbstractUpdateAction {

    private static final String PARAM_REDIRECTPATH = SourceCreatorImpl.PARAM_PREFIX
        + "redirectPath";

    public CreateClassAndTemplateAction(SourceCreatorImpl sourceCreator) {
        super(sourceCreator);
    }

    public Response act(Request request, String className, File sourceFile,
        File templateFile) {

        String subTask = request.getParameter(PARAM_SUBTASK);
        if ("template".equals(subTask)) {
            return actTemplate(request, className, sourceFile, templateFile);
        } else if ("redirect".equals(subTask)) {
            return actRedirect(request, className, sourceFile, templateFile);
        } else {
            return actDefault(request, className, sourceFile, templateFile);
        }
    }

    Response actDefault(Request request, String className, File sourceFile,
        File templateFile) {

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("className", className);
        return getSourceCreator().getResponseCreator().createResponse(
            "createClassAndTemplate", variableMap);
    }

    Response actTemplate(Request request, String className, File sourceFile,
        File templateFile) {

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        String template = getSourceCreator().getSourceGenerator()
            .generateTemplateSource(getSuffix(templateFile.getName()),
                new HashMap());
        if (template == null) {
            template = "";
        }

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("method", method);
        variableMap.put("templateFile", templateFile);
        variableMap.put("template", template);
        return getSourceCreator().getResponseCreator().createResponse(
            "createClassAndTemplate_template", variableMap);
    }

    Response actRedirect(Request request, String className, File sourceFile,
        File templateFile) {

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        String redirectPath = request.getParameter(PARAM_REDIRECTPATH);
        if (redirectPath == null) {
            return null;
        }

        ClassDesc classDesc = new ClassDescImpl(className);
        MethodDesc methodDesc = new MethodDescImpl(getSourceCreator()
            .getActionName(request.getPath(), method));
        methodDesc.setReturnTypeDesc(String.class.getName());
        methodDesc.setBodyDesc(new BodyDescImpl("return "
            + quote("redirect:" + redirectPath) + ";"));
        classDesc.setMethodDesc(methodDesc);
        classDesc.setMethodDesc(new MethodDescImpl(
            DefaultRequestProcessor.ACTION_RENDER));

        writeSourceFile(classDesc);

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("method", method);
        variableMap.put("className", className);
        return getSourceCreator().getResponseCreator().createResponse(
            "createClassAndTemplate_redirect", variableMap);
    }
}
