package org.seasar.cms.framework.creator.action.impl;

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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.creator.BodyDesc;
import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.ClassDescSet;
import org.seasar.cms.framework.creator.DescValidator;
import org.seasar.cms.framework.creator.EntityMetaData;
import org.seasar.cms.framework.creator.MethodDesc;
import org.seasar.cms.framework.creator.ParameterDesc;
import org.seasar.cms.framework.creator.PropertyDesc;
import org.seasar.cms.framework.creator.TypeDesc;
import org.seasar.cms.framework.creator.impl.BodyDescImpl;
import org.seasar.cms.framework.creator.impl.ClassDescImpl;
import org.seasar.cms.framework.creator.impl.MethodDescImpl;
import org.seasar.cms.framework.creator.impl.ParameterDescImpl;
import org.seasar.cms.framework.creator.impl.PropertyDescImpl;
import org.seasar.cms.framework.creator.impl.SimpleClassDesc;
import org.seasar.cms.framework.creator.impl.SourceCreatorImpl;
import org.seasar.cms.framework.creator.impl.TypeDescImpl;
import org.seasar.cms.framework.impl.DefaultRequestProcessor;

public class UpdateClassesAction extends AbstractUpdateAction {

    private static final String PARAM_APPLY = SourceCreatorImpl.PARAM_PREFIX
        + "apply";

    private static final String PARAM_REPLACE = SourceCreatorImpl.PARAM_PREFIX
        + "replace";

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

        ClassDesc[] classDescs = addRelativeClassDescs(gatherClassDescs(request
            .getPath(), request.getMethod(), className, templateFile));
        if (classDescs.length == 0) {
            return null;
        }

        ClassDescBag classDescBag = classifyClassDescs(classDescs);

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
        variableMap.put("templateFile", templateFile);
        variableMap.put("parameters", getParameters(request));
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

    Response actUpdate(Request request, String className, File sourceFile,
        File templateFile) {

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        ClassDesc[] classDescs = addRelativeClassDescs(gatherClassDescs(request
            .getPath(), method, className, templateFile));
        ClassDescBag classDescBag = classifyClassDescs(classDescs);
        ClassDescSet classDescSet = classDescBag.getClassDescSet();

        String[] appliedClassNames = request.getParameterValues(PARAM_APPLY);
        Set appliedClassNameSet = new HashSet();
        if (appliedClassNames != null) {
            appliedClassNameSet.addAll(Arrays.asList(appliedClassNames));
        }
        for (int i = 0; i < classDescs.length; i++) {
            String name = classDescs[i].getName();
            if (!appliedClassNameSet.contains(name)) {
                classDescBag.remove(name);
            }
        }

        boolean mergeMethod = !"true".equals(request
            .getParameter(PARAM_REPLACE));

        writeSourceFiles(classDescBag, ClassDesc.KIND_BEAN, false);
        writeSourceFiles(classDescBag, ClassDesc.KIND_DAO, false);
        writeSourceFiles(classDescBag, ClassDesc.KIND_DTO, mergeMethod);
        writeSourceFiles(classDescBag, ClassDesc.KIND_DXO, mergeMethod);

        ClassDesc[] pageClassDescs = classDescBag
            .getClassDescs(ClassDesc.KIND_PAGE);
        for (int i = 0; i < pageClassDescs.length; i++) {
            // Dtoに触るようなプロパティを持っているなら
            // Dtoに対応するBeanに対応するDaoのsetterを自動生成する。
            // Dxoのsetterも自動生成する。
            // _render()のボディも自動生成する。
            PropertyDesc[] pds = pageClassDescs[i].getPropertyDescs();
            for (int j = 0; j < pds.length; j++) {
                TypeDesc td = pds[j].getTypeDesc();
                if (!DescValidator.isValid(td, classDescSet)
                    || !ClassDesc.KIND_DTO.equals(td.getClassDesc().getKind())) {
                    continue;
                }

                EntityMetaData metaData = new EntityMetaData(
                    getSourceCreator(), td.getClassDesc().getName());
                boolean daoExists = addPropertyIfValid(pageClassDescs[i],
                    new TypeDescImpl(metaData.getDaoClassDesc()),
                    PropertyDesc.WRITE, classDescSet);
                boolean dxoExists = addPropertyIfValid(pageClassDescs[i],
                    new TypeDescImpl(metaData.getDxoClassDesc()),
                    PropertyDesc.WRITE, classDescSet);

                MethodDesc md = pageClassDescs[i]
                    .getMethodDesc(new MethodDescImpl(
                        DefaultRequestProcessor.ACTION_RENDER));
                if (md != null && td.isArray() && pds[j].isReadable()
                    && daoExists && dxoExists) {
                    addSelectStatement(md, pds[j], metaData);
                }
            }
        }
        writeSourceFiles(classDescBag, ClassDesc.KIND_PAGE, mergeMethod);

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("method", method);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("templateFile", templateFile);
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

    void addSelectStatement(MethodDesc methodDesc, PropertyDesc propertyDesc,
        EntityMetaData metaData) {

        BodyDesc bodyDesc = methodDesc.getBodyDesc();
        Map root;
        if (bodyDesc == null) {
            root = new HashMap();
            root.put("entityMetaData", metaData);
            bodyDesc = new BodyDescImpl(DefaultRequestProcessor.ACTION_RENDER,
                root);
        } else {
            root = (Map) bodyDesc.getRoot();
        }
        List propertyDescList = (List) root.get("propertyDescs");
        if (propertyDescList == null) {
            propertyDescList = new ArrayList();
            root.put("propertyDescs", propertyDescList);
        }
        propertyDescList.add(propertyDesc);
        methodDesc.setBodyDesc(bodyDesc);
    }

    boolean addPropertyIfValid(ClassDesc classDesc, TypeDesc typeDesc,
        int mode, ClassDescSet classDescSet) {

        if (DescValidator.isValid(typeDesc, classDescSet)) {
            classDesc.addProperty(typeDesc.getInstanceName(), mode)
                .setTypeDesc(typeDesc);
            return true;
        } else {
            return false;
        }
    }

    void writeSourceFiles(ClassDescBag classDescBag, String kind,
        boolean mergeMethod) {

        ClassDesc[] classDescs = classDescBag.getClassDescs(kind);
        for (int i = 0; i < classDescs.length; i++) {
            // 既存のクラスがあればマージする。
            classDescs[i].merge(getClassDesc(classDescs[i].getName()),
                mergeMethod);
            if (!writeSourceFile(classDescs[i], classDescBag.getClassDescSet())) {
                // ソースファイルの生成に失敗した。
                classDescBag.remove(classDescs[i].getName());
                classDescBag.addAsFailed(classDescs[i]);

            }
        }
    }

    ClassDesc[] addRelativeClassDescs(ClassDesc[] classDescs) {

        Map pageByDtoMap = new HashMap();
        for (int i = 0; i < classDescs.length; i++) {
            if (!classDescs[i].isKindOf(ClassDesc.KIND_PAGE)) {
                continue;
            }
            PropertyDesc[] pds = classDescs[i].getPropertyDescs();
            for (int j = 0; j < pds.length; j++) {
                ClassDesc cd = pds[j].getTypeDesc().getClassDesc();
                if (!cd.isKindOf(ClassDesc.KIND_DTO)) {
                    continue;
                }
                List list = (List) pageByDtoMap.get(cd.getName());
                if (list == null) {
                    list = new ArrayList();
                    pageByDtoMap.put(cd.getName(), list);
                }
                list.add(classDescs[i]);
            }
        }

        List classDescList = new ArrayList(Arrays.asList(classDescs));
        for (int i = 0; i < classDescs.length; i++) {
            if (classDescs[i].isKindOf(ClassDesc.KIND_DTO)) {
                EntityMetaData metaData = new EntityMetaData(
                    getSourceCreator(), classDescs[i].getName());

                // Dao用のClassDescを生成しておく。
                classDescList.add(metaData.getDaoClassDesc());

                // Bean用のClassDescを生成しておく。
                ClassDesc classDesc = metaData.getBeanClassDesc();
                PropertyDesc[] pds = classDescs[i].getPropertyDescs();
                for (int j = 0; j < pds.length; j++) {
                    classDesc.setPropertyDesc((PropertyDesc) pds[j].clone());
                }
                classDescList.add(classDesc);

                // Dxo用のClassDescを生成しておく。
                classDesc = metaData.getDxoClassDesc();
                List list = (List) pageByDtoMap.get(classDescs[i].getName());
                if (list != null) {
                    for (Iterator itr = list.iterator(); itr.hasNext();) {
                        MethodDescImpl md = new MethodDescImpl("convert");
                        ParameterDesc[] pmds = new ParameterDesc[] { new ParameterDescImpl(
                            new TypeDescImpl(((ClassDesc) itr.next()))) };
                        md.setParameterDescs(pmds);
                        md.setReturnTypeDesc(metaData.getBeanClassDesc()
                            .getName());
                        classDesc.setMethodDesc(md);
                    }
                }
                classDescList.add(classDesc);
            }
        }

        return (ClassDesc[]) classDescList.toArray(new ClassDesc[0]);
    }

    ClassDescBag classifyClassDescs(ClassDesc[] classDescs) {

        ClassDescBag classDescBag = new ClassDescBag();
        for (int i = 0; i < classDescs.length; i++) {
            if (getClassDesc(classDescs[i].getName()) == null) {
                classDescBag.addAsCreated(classDescs[i]);
            } else {
                classDescBag.addAsUpdated(classDescs[i]);
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
            classDesc = new ClassDescImpl(className);
            classDescriptorMap.put(className, classDesc);
        }
        if (classDesc != null) {
            classDesc.setMethodDesc(new MethodDescImpl(getSourceCreator()
                .getActionName(path, method)));
            MethodDesc methodDesc = new MethodDescImpl(
                DefaultRequestProcessor.ACTION_RENDER);
            classDesc.setMethodDesc(methodDesc);
        }

        return (ClassDesc[]) classDescriptorMap.values().toArray(
            new ClassDesc[0]);
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

        ClassDesc classDesc = new ClassDescImpl(className);

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
            PropertyDesc propertyDesc = new PropertyDescImpl(name);
            int mode = PropertyDesc.NONE;
            if (pds[i].getReadMethod() != null) {
                mode |= PropertyDesc.READ;
            }
            if (pds[i].getWriteMethod() != null) {
                mode |= PropertyDesc.WRITE;
            }
            propertyDesc.setMode(mode);
            Class propertyType = pds[i].getPropertyType();
            if (propertyType == null) {
                System.out.println("**** PropertyType is NULL: name=" + name);
                continue;
            }

            TypeDesc propertyTypeDesc = propertyDesc.getTypeDesc();
            String componentType;
            if (propertyType.isArray()) {
                componentType = propertyType.getComponentType().getName();
                propertyTypeDesc.setArray(true);
            } else {
                componentType = propertyType.getName();
            }
            propertyTypeDesc.setClassDesc(new SimpleClassDesc(componentType));
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
            MethodDesc methodDesc = new MethodDescImpl(name);
            methodDesc.getReturnTypeDesc().setClassDesc(
                new SimpleClassDesc(methods[i].getReturnType().getName()));
            classDesc.setMethodDesc(methodDesc);
        }

        return classDesc;
    }

    private static class ClassDescBag {
        private Map map_ = new HashMap();

        private Map createdMap_ = new HashMap();

        private Map updatedMap_ = new HashMap();

        private Map failedMap_ = new LinkedHashMap();

        private ClassDescSet set_ = new ClassDescSet();

        public Map getClassDescMap() {
            return getClassDescMap(map_, null);
        }

        public ClassDesc[] getClassDescs() {
            return getClassDescs(map_, null);
        }

        public Map getClassDescMap(String kind) {
            return getClassDescMap(map_, kind);
        }

        public ClassDesc[] getClassDescs(String kind) {
            return (ClassDesc[]) getClassDescMap(kind).values().toArray(
                new ClassDesc[0]);
        }

        public Iterator getClassDescKindIterator() {
            return map_.keySet().iterator();
        }

        public Map getCreatedClassDescMap() {
            return getClassDescMap(createdMap_, null);
        }

        public ClassDesc[] getCreatedClassDescs() {
            return (ClassDesc[]) getCreatedClassDescMap().values().toArray(
                new ClassDesc[0]);
        }

        public Map getCreatedClassDescMap(String kind) {
            return getClassDescMap(createdMap_, kind);
        }

        public ClassDesc[] getCreatedClassDescs(String kind) {
            return (ClassDesc[]) getCreatedClassDescMap(kind).values().toArray(
                new ClassDesc[0]);
        }

        public Map getUpdatedClassDescMap() {
            return getClassDescMap(updatedMap_, null);
        }

        public ClassDesc[] getUpdatedClassDescs() {
            return (ClassDesc[]) getUpdatedClassDescMap().values().toArray(
                new ClassDesc[0]);
        }

        public Map getUpdatedClassDescMap(String kind) {
            return getClassDescMap(updatedMap_, kind);
        }

        public ClassDesc[] getUpdatedClassDescs(String kind) {
            return (ClassDesc[]) getUpdatedClassDescMap(kind).values().toArray(
                new ClassDesc[0]);
        }

        Map getClassDescMap(Map map, String kind) {
            Map got = (Map) map.get(kind);
            if (got == null) {
                got = new LinkedHashMap();
                map.put(kind, got);
            }
            return got;
        }

        ClassDesc[] getClassDescs(Map map, String kind) {
            return (ClassDesc[]) getClassDescMap(map, kind).values().toArray(
                new ClassDesc[0]);
        }

        public ClassDesc[] getFailedClassDescs() {
            return (ClassDesc[]) failedMap_.values().toArray(new ClassDesc[0]);
        }

        public ClassDescSet getClassDescSet() {
            return set_;
        }

        public void addAsCreated(ClassDesc classDesc) {
            getClassDescMap().put(classDesc.getName(), classDesc);
            getClassDescMap(classDesc.getKind()).put(classDesc.getName(),
                classDesc);
            getCreatedClassDescMap().put(classDesc.getName(), classDesc);
            getCreatedClassDescMap(classDesc.getKind()).put(
                classDesc.getName(), classDesc);
            set_.add(classDesc);
        }

        public void addAsUpdated(ClassDesc classDesc) {
            getClassDescMap().put(classDesc.getName(), classDesc);
            getClassDescMap(classDesc.getKind()).put(classDesc.getName(),
                classDesc);
            getUpdatedClassDescMap().put(classDesc.getName(), classDesc);
            getUpdatedClassDescMap(classDesc.getKind()).put(
                classDesc.getName(), classDesc);
            set_.add(classDesc);
        }

        public void addAsFailed(ClassDesc classDesc) {
            failedMap_.put(classDesc.getName(), classDesc);
        }

        public void remove(String className) {
            remove(map_, className);
            remove(createdMap_, className);
            remove(updatedMap_, className);
            set_.remove(className);
        }

        void remove(Map map, String className) {
            for (Iterator itr = map.values().iterator(); itr.hasNext();) {
                Map kindMap = (Map) itr.next();
                kindMap.remove(className);
            }
        }
    }
}
