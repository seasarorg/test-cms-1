package org.seasar.cms.ymir.extension.creator.action.impl;

import static org.seasar.cms.ymir.impl.DefaultRequestProcessor.PARAM_METHOD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.ClassDescBag;
import org.seasar.cms.ymir.extension.creator.PathMetaData;
import org.seasar.cms.ymir.extension.creator.PropertyTypeHint;
import org.seasar.cms.ymir.extension.creator.PropertyTypeHintBag;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.Template;
import org.seasar.cms.ymir.extension.creator.action.UpdateAction;
import org.seasar.cms.ymir.impl.DefaultRequestProcessor;
import org.seasar.kvasir.util.PropertyUtils;

public class UpdateClassesAction extends AbstractAction implements UpdateAction {

    protected static final String PARAM_APPLY = SourceCreator.PARAM_PREFIX
            + "apply";

    protected static final String PARAM_REPLACE = SourceCreator.PARAM_PREFIX
            + "replace";

    protected static final String PARAMPREFIX_PROPERTYTYPE = SourceCreator.PARAM_PREFIX
            + "propertyType_";

    protected static final String PREFIX_CHECKEDTIME = "updateClassesAction.checkedTime.";

    protected static final String PREFIX_CLASSCHECKED = "updateClassesAction.class.checked.";

    private static final String SUFFIX_ARRAY = "[]";

    private static final String PACKAGEPREFIX_JAVA_LANG = "java.lang.";

    private static final Set<String> primitiveSet_;

    static {
        Set<String> primitiveSet = new HashSet<String>();
        primitiveSet.addAll(Arrays.asList(new String[] { "boolean", "byte",
            "char", "short", "int", "long", "float", "double" }));
        primitiveSet_ = Collections.unmodifiableSet(primitiveSet);
    }

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
                    || ClassDesc.KIND_BEAN.equals(kind)
                    || ClassDesc.KIND_DXO.equals(kind)) {
                dtos[i] = new ClassDescDto(classDescs[i], false);
            } else {
                dtos[i] = new ClassDescDto(classDescs[i], PropertyUtils
                        .valueOf(prop.getProperty(PREFIX_CLASSCHECKED + name),
                                true));
            }
        }

        return dtos;
    }

    Response actUpdate(Request request, PathMetaData pathMetaData) {

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        List<PropertyTypeHint> hintList = new ArrayList<PropertyTypeHint>();
        for (Iterator itr = request.getParameterNames(); itr.hasNext();) {
            String name = (String) itr.next();
            if (!name.startsWith(PARAMPREFIX_PROPERTYTYPE)) {
                continue;
            }
            String classAndPropertyName = name
                    .substring(PARAMPREFIX_PROPERTYTYPE.length());
            int slash = classAndPropertyName.indexOf('/');
            if (slash < 0) {
                continue;
            }
            String className = classAndPropertyName.substring(0, slash);
            String propertyName = classAndPropertyName.substring(slash + 1);
            String typeName = request.getParameter(name);
            boolean array;
            if (typeName.endsWith(SUFFIX_ARRAY)) {
                array = true;
                typeName = typeName.substring(0, typeName.length()
                        - SUFFIX_ARRAY.length());
            } else {
                array = false;
            }
            typeName = normalizeTypeName(typeName);
            hintList.add(new PropertyTypeHint(className, propertyName,
                    typeName, array));
        }

        ClassDescBag classDescBag = getSourceCreator().gatherClassDescs(
                new PathMetaData[] { pathMetaData },
                new PropertyTypeHintBag(hintList
                        .toArray(new PropertyTypeHint[0])), null);

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

    String normalizeTypeName(String typeName) {
        if (typeName == null || typeName.indexOf('.') >= 0
                || primitiveSet_.contains(typeName)) {
            return typeName;
        } else {
            return PACKAGEPREFIX_JAVA_LANG + typeName;
        }
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
