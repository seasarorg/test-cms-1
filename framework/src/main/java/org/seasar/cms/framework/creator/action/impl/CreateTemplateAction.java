package org.seasar.cms.framework.creator.action.impl;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.creator.impl.SourceCreatorImpl;

public class CreateTemplateAction extends AbstractUpdateAction {

    private static final String PARAM_TEMPLATE = SourceCreatorImpl.PARAM_PREFIX
        + "template";

    public CreateTemplateAction(SourceCreatorImpl sourceCreator) {
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

        if (!isOwnTemplateUsed(request.getPath(), request.getMethod())) {
            return null;
        }

        String actionName = getSourceCreator().getActionName(request.getPath(),
            request.getMethod());

        String template = getSourceCreator().getSourceGenerator()
            .generateTemplateSource(getSuffix(templateFile.getName()),
                new HashMap());
        if (template == null) {
            template = "";
        }

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("className", className);
        variableMap.put("sourceFile", sourceFile);
        variableMap.put("templateFile", templateFile);
        variableMap.put("actionName", actionName);
        variableMap.put("template", template);
        return getSourceCreator().getResponseCreator().createResponse(
            "createTemplate", variableMap);
    }

    boolean isOwnTemplateUsed(String path, String method) {

        Class pageClass = getSourceCreator().getClass(
            getSourceCreator().getClassName(
                getSourceCreator().getComponentName(path, method)));
        if (pageClass == null) {
            return true;
        }
        Method actionMethod;
        try {
            actionMethod = pageClass.getMethod(getSourceCreator()
                .getActionName(path, method), new Class[0]);
        } catch (SecurityException ex) {
            return true;
        } catch (NoSuchMethodException ex) {
            return true;
        }
        if (actionMethod.getReturnType() == Void.TYPE) {
            return true;
        } else {
            return false;
        }
    }

    Response actCreate(Request request, String className, File sourceFile,
        File templateFile) {

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        String template = request.getParameter(PARAM_TEMPLATE);
        if (template == null) {
            return null;
        }

        templateFile.getParentFile().mkdirs();
        getSourceCreator().writeString(template, templateFile);

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("method", method);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("templateFile", templateFile);
        return getSourceCreator().getResponseCreator().createResponse(
            "createTemplate_create", variableMap);
    }
}
