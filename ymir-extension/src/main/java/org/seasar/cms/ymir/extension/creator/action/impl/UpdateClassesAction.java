package org.seasar.cms.ymir.extension.creator.action.impl;

import static org.seasar.cms.ymir.impl.DefaultRequestProcessor.PARAM_METHOD;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.ClassDescBag;
import org.seasar.cms.ymir.extension.creator.PathMetaData;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.Template;
import org.seasar.cms.ymir.impl.DefaultRequestProcessor;
import org.seasar.kvasir.util.PropertyUtils;

public class UpdateClassesAction extends AbstractUpdateAction {

    protected static final String PARAM_APPLY = SourceCreator.PARAM_PREFIX
            + "apply";

    protected static final String PARAM_REPLACE = SourceCreator.PARAM_PREFIX
            + "replace";

    protected static final String PREFIX_CHECKEDTIME = "updateClassesAction.checkedTime.";

    protected static final String PREFIX_CLASSCHECKED = "updateClassesAction.class.checked.";

    public UpdateClassesAction(SourceCreator sourceCreator) {
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
        variableMap.put("template", pathMetaData.getTemplate());
        variableMap.put("parameters", getParameters(request));
        variableMap.put("pathMetaData", pathMetaData);
        variableMap.put("createdClassDescs", createClassDescDtos(classDescBag
                .getCreatedClassDescs()));
        variableMap.put("updatedClassDescs", createClassDescDtos(classDescBag
                .getUpdatedClassDescs()));
        return getSourceCreator().getResponseCreator().createResponse(
                "updateClasses", variableMap);
    }

    protected ClassDescDto[] createClassDescDtos(ClassDesc[] classDescs) {

        Properties prop = getSourceCreator().getSourceCreatorProperties();

        ClassDescDto[] dtos = new ClassDescDto[classDescs.length];
        for (int i = 0; i < classDescs.length; i++) {
            String name = classDescs[i].getName();
            String kind = classDescs[i].getKind();
            if (ClassDesc.KIND_DAO.equals(kind)
                    || ClassDesc.KIND_BEAN.equals(kind)) {
                dtos[i] = new ClassDescDto(name, false);
            } else {
                dtos[i] = new ClassDescDto(name, PropertyUtils.valueOf(prop
                        .getProperty(PREFIX_CLASSCHECKED + name), true));
            }
        }

        return dtos;
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
        Properties prop = getSourceCreator().getSourceCreatorProperties();
        ClassDesc[] classDescs = classDescBag.getClassDescs();
        for (int i = 0; i < classDescs.length; i++) {
            String name = classDescs[i].getName();
            String checked;
            if (appliedClassNameSet.contains(name)) {
                checked = String.valueOf(true);
            } else {
                checked = String.valueOf(false);
                classDescBag.remove(name);
            }
            prop.setProperty(PREFIX_CLASSCHECKED + name, checked);
        }
        getSourceCreator().saveSourceCreatorProperties();

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
        variableMap.put("suggestionExists", Boolean.valueOf(classDescBag
                .getClassDescMap(ClassDesc.KIND_PAGE).size()
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

        Template template = pathMetaData.getTemplate();
        if (template == null || !template.exists()) {
            return false;
        }
        boolean shouldUpdate = (template.lastModified() > getCheckedTime(template));
        if (shouldUpdate) {
            updateCheckedTime(template);
        }
        return shouldUpdate;
    }

    long getCheckedTime(Template template) {

        Properties prop = getSourceCreator().getSourceCreatorProperties();
        String key = PREFIX_CHECKEDTIME + template.getPath();
        String timeString = prop.getProperty(key);
        long time;
        if (timeString == null) {
            time = 0L;
        } else {
            time = Long.parseLong(timeString);
        }

        return time;
    }

    void updateCheckedTime(Template template) {

        Properties prop = getSourceCreator().getSourceCreatorProperties();
        String key = PREFIX_CHECKEDTIME + template.getPath();
        prop.setProperty(key, String.valueOf(System.currentTimeMillis()));
        getSourceCreator().saveSourceCreatorProperties();
    }
}
