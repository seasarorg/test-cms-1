package org.seasar.cms.framework.creator.action.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.ClassDescBag;
import org.seasar.cms.framework.creator.PathMetaData;
import org.seasar.cms.framework.creator.impl.SourceCreatorImpl;
import org.seasar.cms.framework.impl.DefaultRequestProcessor;

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

        List createdClassDescList = new ArrayList();
        for (Iterator itr = classDescBag.getCreatedClassDescMap().values()
            .iterator(); itr.hasNext();) {
            ClassDesc classDesc = (ClassDesc) itr.next();
            String kind = classDesc.getKind();
            if (ClassDesc.KIND_DAO.equals(kind)
                || ClassDesc.KIND_BEAN.equals(kind)) {
                continue;
            }
            createdClassDescList.add(classDesc);
        }

        List updatedClassDescList = new ArrayList();
        for (Iterator itr = classDescBag.getUpdatedClassDescMap().values()
            .iterator(); itr.hasNext();) {
            ClassDesc classDesc = (ClassDesc) itr.next();
            String kind = classDesc.getKind();
            if (ClassDesc.KIND_DAO.equals(kind)
                || ClassDesc.KIND_BEAN.equals(kind)) {
                continue;
            }
            updatedClassDescList.add(classDesc);
        }

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("templateFile", pathMetaData.getTemplateFile());
        variableMap.put("parameters", getParameters(request));
        variableMap.put("pathMetaData", pathMetaData);
        variableMap.put("createdClassDescs", createdClassDescList);
        variableMap.put("updatedClassDescs", updatedClassDescList);
        variableMap.put("daoClassDescExists", Boolean.valueOf(classDescBag
            .getClassDescMap(ClassDesc.KIND_DAO).size()
            + classDescBag.getClassDescMap(ClassDesc.KIND_BEAN).size() > 0));
        variableMap.put("createdDaoClassDescs", classDescBag
            .getCreatedClassDescs(ClassDesc.KIND_DAO));
        variableMap.put("updatedDaoClassDescs", classDescBag
            .getUpdatedClassDescs(ClassDesc.KIND_DAO));
        variableMap.put("createdBeanClassDescs", classDescBag
            .getCreatedClassDescs(ClassDesc.KIND_BEAN));
        variableMap.put("updatedBeanClassDescs", classDescBag
            .getUpdatedClassDescs(ClassDesc.KIND_BEAN));
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
        if (classDescBag.isEmpty()) {
            return null;
        }

        String[] appliedClassNames = request.getParameterValues(PARAM_APPLY);
        Set appliedClassNameSet = new HashSet();
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

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("method", method);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("pathMetaData", pathMetaData);
        variableMap.put("createdClassDescs", classDescBag
            .getCreatedClassDescs());
        variableMap.put("updatedClassDescs", classDescBag
            .getUpdatedClassDescs());
        variableMap.put("failedClassDescs", classDescBag.getFailedClassDescs());
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
