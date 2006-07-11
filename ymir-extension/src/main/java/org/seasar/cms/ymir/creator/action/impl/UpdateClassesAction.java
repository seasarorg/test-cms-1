package org.seasar.cms.ymir.creator.action.impl;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.creator.ClassDesc;
import org.seasar.cms.ymir.creator.ClassDescBag;
import org.seasar.cms.ymir.creator.PathMetaData;
import org.seasar.cms.ymir.creator.impl.SourceCreatorImpl;
import org.seasar.cms.ymir.impl.DefaultRequestProcessor;

public class UpdateClassesAction extends AbstractUpdateAction {

    private static final String PARAM_APPLY = SourceCreatorImpl.PARAM_PREFIX
        + "apply";

    private static final String PARAM_REPLACE = SourceCreatorImpl.PARAM_PREFIX
        + "replace";

    public UpdateClassesAction(SourceCreatorImpl sourceCreator) {
        super(sourceCreator);
    }

    public Response act(Request request, PathMetaData pathMetaData) {

        String subTask = request.getParameter(PARAM_SUBTASK);
        if ("update".equals(subTask)) {
            return actUpdate(request, pathMetaData);
        } else {
            return actDefault(request, pathMetaData);
        }
    }

    Response actDefault(Request request, PathMetaData pathMetaData) {

        if (!shouldUpdate(pathMetaData)) {
            return null;
        }

        ClassDescBag classDescBag = getSourceCreator().gatherClassDescs(
            new PathMetaData[] { pathMetaData });
        if (classDescBag.isEmpty()) {
            return null;
        }

        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("request", request);
        variableMap.put("templateFile", pathMetaData.getTemplateFile());
        variableMap.put("parameters", getParameters(request));
        variableMap.put("pathMetaData", pathMetaData);
        variableMap.put("classDescBag", classDescBag);
        return getSourceCreator().getResponseCreator().createResponse(
            "updateClasses", variableMap);
    }

    Response actUpdate(Request request, PathMetaData pathMetaData) {

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        ClassDescBag classDescBag = getSourceCreator().gatherClassDescs(
            new PathMetaData[] { pathMetaData });

        String[] appliedClassNames = request.getParameterValues(PARAM_APPLY);
        Set<String> appliedClassNameSet = new HashSet<String>();
        if (appliedClassNames != null) {
            appliedClassNameSet.addAll(Arrays.asList(appliedClassNames));
        }
        ClassDesc[] classDescs = classDescBag.getClassDescs();
        for (int i = 0; i < classDescs.length; i++) {
            String name = classDescs[i].getName();
            if (!appliedClassNameSet.contains(name)) {
                classDescBag.remove(name);
            }
        }

        boolean mergeMethod = !"true".equals(request
            .getParameter(PARAM_REPLACE));

        getSourceCreator().updateClasses(classDescBag, mergeMethod);

        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("request", request);
        variableMap.put("method", method);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("pathMetaData", pathMetaData);
        variableMap.put("classDescBag", classDescBag);
        variableMap.put("actionName", getSourceCreator().getActionName(
            request.getPath(), method));
        variableMap.put("suggestionExists", Boolean
            .valueOf(classDescBag.getClassDescMap(ClassDesc.KIND_PAGE).size()
                + classDescBag.getCreatedClassDescMap(ClassDesc.KIND_BEAN)
                    .size() > 0));
        variableMap.put("pageClassDescs", classDescBag
            .getClassDescs(ClassDesc.KIND_PAGE));
        variableMap.put("renderActionName",
            DefaultRequestProcessor.ACTION_RENDER);
        variableMap.put("createdBeanClassDescs", classDescBag
            .getCreatedClassDescs(ClassDesc.KIND_BEAN));
        return getSourceCreator().getResponseCreator().createResponse(
            "updateClasses_update", variableMap);
    }

    boolean shouldUpdate(PathMetaData pathMetaData) {

        File templateFile = pathMetaData.getTemplateFile();
        if (!templateFile.exists()) {
            return false;
        }
        File sourceFile = pathMetaData.getSourceFile();
        return (!sourceFile.exists() || templateFile.lastModified() > sourceFile
            .lastModified());
    }
}
