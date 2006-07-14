package org.seasar.cms.ymir.creator.action.impl;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.creator.PathMetaData;
import org.seasar.cms.ymir.creator.impl.SourceCreatorImpl;

public class CreateTemplateAction extends AbstractUpdateAction {

    private static final String PARAM_TEMPLATE = SourceCreatorImpl.PARAM_PREFIX
        + "template";

    public CreateTemplateAction(SourceCreatorImpl sourceCreator) {
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

        if (!isOwnTemplateUsed(pathMetaData)) {
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
        variableMap.put("parameters", getParameters(request));
        variableMap.put("pathMetaData", pathMetaData);
        variableMap.put("template", template);
        return getSourceCreator().getResponseCreator().createResponse(
            "createTemplate", variableMap);
    }

    boolean isOwnTemplateUsed(PathMetaData pathMetaData) {

        Class pageClass = getSourceCreator().getClass(
            pathMetaData.getClassName());
        if (pageClass == null) {
            return true;
        }
        Method actionMethod;
        try {
            actionMethod = pageClass.getMethod(pathMetaData.getActionName(),
                new Class[0]);
        } catch (SecurityException ex) {
            return true;
        } catch (NoSuchMethodException ex) {
            return true;
        }
        if (actionMethod.getReturnType() == Void.TYPE) {
            return (pathMetaData.getDefaultPath() == null);
        } else {
            return false;
        }
    }

    Response actCreate(Request request, PathMetaData pathMetaData) {

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        String template = request.getParameter(PARAM_TEMPLATE);
        if (template == null) {
            return null;
        }

        File templateFile = pathMetaData.getTemplateFile();
        templateFile.getParentFile().mkdirs();
        getSourceCreator().writeString(template, templateFile);

        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("request", request);
        variableMap.put("method", method);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("pathMetaData", pathMetaData);
        return getSourceCreator().getResponseCreator().createResponse(
            "createTemplate_create", variableMap);
    }
}
