package org.seasar.cms.framework.creator.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.MethodDesc;
import org.seasar.cms.framework.creator.PropertyDesc;
import org.seasar.cms.framework.impl.DefaultRequestProcessor;

public class UpdateClassesAction extends AbstractUpdateAction {

    private static final String PARAM_DAO = SourceCreatorImpl.PARAM_PREFIX
        + "dao";

    public UpdateClassesAction(SourceCreatorImpl sourceCreator) {
        super(sourceCreator);
    }

    public Response act(Request request, String className, File sourceFile,
        File templateFile) {

        String subTask = request.getParameter(PARAM_SUBTASK);
        if ("update".equals(subTask)) {
            return actUpdate(request, className, sourceFile, templateFile);
        } else {
            return actDefault(request, className, sourceFile, templateFile);
        }
    }

    Response actDefault(Request request, String className, File sourceFile,
        File templateFile) {

        if (!shouldUpdate(sourceFile, templateFile)) {
            return null;
        }

        ClassDesc[] classDescs = gatherClassDescs(request.getPath(), request
            .getMethod(), className, templateFile);
        if (classDescs.length == 0) {
            return null;
        }

        ClassDescBag classDescBag = classifyClassDescs(classDescs);

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("templateFile", templateFile);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("createdClassDescs", classDescBag.getCreatedList());
        variableMap.put("updatedClassDescs", classDescBag.getUpdatedList());
        variableMap.put("daoClassDescExists", Boolean.valueOf(classDescBag
            .getDaoList().size() > 0));
        variableMap.put("createdDaoClassDescs", classDescBag
            .getCreatedDaoList());
        variableMap.put("updatedDaoClassDescs", classDescBag
            .getUpdatedDaoList());
        return getSourceCreator().getResponseCreator().createResponse(
            "updateClasses", variableMap);
    }

    Response actUpdate(Request request, String className, File sourceFile,
        File templateFile) {

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        ClassDesc[] classDescs = gatherClassDescs(request.getPath(), method,
            className, templateFile);
        for (int i = 0; i < classDescs.length; i++) {
            writeSourceFile(classDescs[i], true);
        }

        ClassDescBag classDescBag = classifyClassDescs(classDescs);

        Set daoClassNameSet = new HashSet();
        String[] daoClassNames = request.getParameterValues(PARAM_DAO);
        if (daoClassNames != null) {
            daoClassNameSet.addAll(Arrays.asList(daoClassNames));
        }

        ClassDesc[] daoClassDescs = (ClassDesc[]) classDescBag.getDaoList()
            .toArray(new ClassDesc[0]);
        for (int i = 0; i < daoClassDescs.length; i++) {
            if (daoClassNameSet.contains(daoClassDescs[i].getName())) {
                writeSourceFile(daoClassDescs[i], false);
            }
        }

        classDescBag.getCreatedList().addAll(classDescBag.getCreatedDaoList());
        classDescBag.getUpdatedList().addAll(classDescBag.getUpdatedDaoList());

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("method", method);
        variableMap.put("templateFile", templateFile);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("createdClassDescs", classDescBag.getCreatedList());
        variableMap.put("updatedClassDescs", classDescBag.getUpdatedList());
        return getSourceCreator().getResponseCreator().createResponse(
            "updateClasses_update", variableMap);
    }

    ClassDescBag classifyClassDescs(ClassDesc[] classDescs) {

        ClassDescBag classDescBag = new ClassDescBag();
        for (int i = 0; i < classDescs.length; i++) {
            if (getClassDesc(classDescs[i].getName()) == null) {
                classDescBag.addAsCreated(classDescs[i]);
            } else {
                classDescBag.addAsUpdated(classDescs[i]);
            }

            if (ClassDesc.KIND_DTO.equals(classDescs[i].getKind())) {
                ClassDesc classDesc = (ClassDesc) classDescs[i].clone();
                classDesc.setName(getSourceCreator().getDaoPackageName() + "."
                    + classDescs[i].getBaseName() + "Dao");
                classDesc.setKind(ClassDesc.KIND_DAO);
                if (getClassDesc(classDesc.getName()) == null) {
                    classDescBag.addAsCreatedDao(classDesc);
                } else {
                    classDescBag.addAsUpdatedDao(classDesc);
                }

                classDesc = (ClassDesc) classDescs[i].clone();
                classDesc.setName(getSourceCreator().getDaoPackageName() + "."
                    + classDescs[i].getBaseName());
                classDesc.setKind(ClassDesc.KIND_BEAN);
                if (getClassDesc(classDesc.getName()) == null) {
                    classDescBag.addAsCreatedDao(classDesc);
                } else {
                    classDescBag.addAsUpdatedDao(classDesc);
                }
            }
        }

        return classDescBag;
    }

    boolean shouldUpdate(File sourceFile, File templateFile) {

        if (!templateFile.exists()) {
            return false;
        }
        return (!sourceFile.exists() || templateFile.lastModified() > sourceFile
            .lastModified());
    }

    ClassDesc[] update(String path, String method, String className,
        File templateFile) {

        ClassDesc[] classDescs = gatherClassDescs(path, method, className,
            templateFile);
        for (int i = 0; i < classDescs.length; i++) {
            writeSourceFile(classDescs[i], true);
        }
        return classDescs;
    }

    ClassDesc[] gatherClassDescs(String path, String method, String className,
        File templateFile) {

        Map classDescriptorMap = new LinkedHashMap();
        try {
            getSourceCreator().getTemplateAnalyzer().analyze(method,
                classDescriptorMap, new FileInputStream(templateFile),
                getSourceCreator().getEncoding(), className);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        ClassDesc classDesc = (ClassDesc) classDescriptorMap.get(className);
        if (classDesc == null && method.equalsIgnoreCase(Request.METHOD_POST)) {
            // テンプレートを解析した結果対応するPageクラスを作る必要があると
            // 見なされなかった場合でも、methodがPOSTならPageクラスを作る。
            classDesc = new ClassDesc(className);
            classDescriptorMap.put(className, classDesc);
        }
        if (classDesc != null) {
            classDesc.setMethodDesc(new MethodDesc(getSourceCreator()
                .getActionName(path, method)));
            classDesc.setMethodDesc(new MethodDesc(
                DefaultRequestProcessor.ACTION_RENDER));
        }

        return (ClassDesc[]) classDescriptorMap.values().toArray(
            new ClassDesc[0]);
    }

    void writeSourceFile(ClassDesc classDesc, boolean merge) {

        if (merge) {
            classDesc.merge(getClassDesc(classDesc.getName()));
        }
        writeSourceFile(classDesc);
    }

    ClassDesc getClassDesc(String className) {

        Class clazz = getSourceCreator().getClass(className);
        if (clazz == null) {
            return null;
        }

        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }

        ClassDesc classDesc = new ClassDesc(className);

        Class superclass = clazz.getSuperclass();
        // Generation-GapのBaseクラスを飛ばすため。
        if (superclass != null) {
            superclass = superclass.getSuperclass();
        }
        if (superclass != null && superclass != Object.class) {
            classDesc.setSuperclassName(superclass.getName());
        }

        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < pds.length; i++) {
            String name = pds[i].getName();
            if ("class".equals(name)) {
                continue;
            }
            PropertyDesc propertyDesc = new PropertyDesc(name);
            int mode = PropertyDesc.NONE;
            if (pds[i].getReadMethod() != null) {
                mode |= PropertyDesc.READ;
            }
            if (pds[i].getWriteMethod() != null) {
                mode |= PropertyDesc.WRITE;
            }
            propertyDesc.setMode(mode);
            Class propertyType = pds[i].getPropertyType();
            String type;
            if (propertyType.isArray()) {
                type = propertyType.getComponentType().getName() + "[]";
            } else {
                type = propertyType.getName();
            }
            propertyDesc.setType(type);
            propertyDesc.setMode(mode);
            classDesc.setPropertyDesc(propertyDesc);
        }

        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getParameterTypes().length > 0) {
                continue;
            }
            String name = methods[i].getName();
            if (name.startsWith("get") || name.startsWith("is")
                || name.startsWith("set")) {
                continue;
            }
            if (methods[i].getDeclaringClass() == Object.class) {
                continue;
            }
            MethodDesc methodDesc = new MethodDesc(name);
            methodDesc.setReturnType(methods[i].getReturnType().getName());
            classDesc.setMethodDesc(methodDesc);
        }

        return classDesc;
    }

    private static class ClassDescBag {
        private List createdList_ = new ArrayList();

        private List updatedList_ = new ArrayList();

        private List createdDaoList_ = new ArrayList();

        private List updatedDaoList_ = new ArrayList();

        private List daoList_ = new ArrayList();

        public List getCreatedList() {
            return createdList_;
        }

        public List getDaoList() {
            return daoList_;
        }

        public void addAsCreated(ClassDesc classDesc) {
            createdList_.add(classDesc);
        }

        public List getUpdatedList() {
            return updatedList_;
        }

        public void addAsUpdated(ClassDesc classDesc) {
            updatedList_.add(classDesc);
        }

        public List getCreatedDaoList() {
            return createdDaoList_;
        }

        public void addAsCreatedDao(ClassDesc classDesc) {
            createdDaoList_.add(classDesc);
            daoList_.add(classDesc);
        }

        public List getUpdatedDaoList() {
            return updatedDaoList_;
        }

        public void addAsUpdatedDao(ClassDesc classDesc) {
            updatedDaoList_.add(classDesc);
            daoList_.add(classDesc);
        }
    }
}
