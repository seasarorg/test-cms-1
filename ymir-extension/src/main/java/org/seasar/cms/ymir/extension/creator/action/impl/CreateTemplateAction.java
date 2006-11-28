package org.seasar.cms.ymir.extension.creator.action.impl;

import static org.seasar.cms.ymir.extension.creator.SourceCreator.PARAM_PREFIX;
import static org.seasar.cms.ymir.impl.DefaultRequestProcessor.PARAM_METHOD;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.MethodDesc;
import org.seasar.cms.ymir.extension.creator.PathMetaData;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.Template;
import org.seasar.cms.ymir.extension.creator.impl.BodyDescImpl;
import org.seasar.cms.ymir.extension.creator.impl.MethodDescImpl;

public class CreateTemplateAction extends AbstractUpdateAction {

    private static final String PARAM_TEMPLATE = PARAM_PREFIX + "template";

    private static final String PARAM_TRANSITION = PARAM_PREFIX + "transition";

    private static final String PARAM_TRANSITIONREDIRECT = PARAM_TRANSITION
            + "_redirect";

    public CreateTemplateAction(SourceCreator sourceCreator) {
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

        boolean actionMethodNotFound = false;
        Class pageClass = getSourceCreator().getClass(
                pathMetaData.getClassName());
        if (pageClass != null) {
            Method actionMethod = getActionMethod(pageClass, pathMetaData
                    .getActionName());
            if (actionMethod == null) {
                actionMethodNotFound = true;
            }
        }

        String template = getSourceCreator().getSourceGenerator()
                .generateTemplateSource(
                        getSuffix(pathMetaData.getTemplate().getName()),
                        new HashMap());
        if (template == null) {
            template = "";
        }

        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("request", request);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("pathMetaData", pathMetaData);
        variableMap.put("template", template);
        variableMap.put("actionMethodNotFound", actionMethodNotFound);
        return getSourceCreator().getResponseCreator().createResponse(
                "createTemplate", variableMap);
    }

    Method getActionMethod(Class pageClass, String actionName) {
        try {
            return pageClass.getMethod(actionName, new Class[0]);
        } catch (SecurityException ex) {
            return null;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    Response actCreate(Request request, PathMetaData pathMetaData) {

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        String templateString = request.getParameter(PARAM_TEMPLATE);
        String transition = request.getParameter(PARAM_TRANSITION);
        boolean redirect = "true".equals(request
                .getParameter(PARAM_TRANSITIONREDIRECT));
        if (templateString != null) {
            Template template = pathMetaData.getTemplate();
            try {
                getSourceCreator().writeString(templateString,
                        template.getOutputStream());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else if (transition != null) {
            ClassDesc classDesc = getSourceCreator().newClassDesc(
                    pathMetaData.getClassName());
            MethodDesc methodDesc = new MethodDescImpl(getSourceCreator()
                    .getActionName(request.getPath(), method));
            methodDesc.setReturnTypeDesc(String.class.getName(), true);
            if (redirect) {
                transition = "redirect:" + transition;
            }
            methodDesc.setBodyDesc(new BodyDescImpl("return "
                    + quote(transition.trim()) + ";"));
            classDesc.setMethodDesc(methodDesc);
            getSourceCreator().mergeWithExistentClass(classDesc, true);
            getSourceCreator().writeSourceFile(classDesc, null);
        } else {
            return null;
        }

        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("request", request);
        variableMap.put("method", method);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("pathMetaData", pathMetaData);
        variableMap.put("templateCreatd", (templateString != null));
        variableMap.put("transitionSet", (transition != null));
        variableMap.put("transition", transition);
        return getSourceCreator().getResponseCreator().createResponse(
                "createTemplate_create", variableMap);
    }
}
