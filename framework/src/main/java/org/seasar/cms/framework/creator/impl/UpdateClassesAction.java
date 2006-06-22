package org.seasar.cms.framework.creator.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.MethodDesc;
import org.seasar.cms.framework.creator.PropertyDesc;
import org.seasar.cms.framework.impl.DefaultRequestProcessor;

public class UpdateClassesAction extends AbstractUpdateAction {

    public UpdateClassesAction(SourceCreatorImpl sourceCreator) {
        super(sourceCreator);
    }

    public Response act(Request request, String className, File sourceFile,
        File templateFile) {

        if (!shouldUpdate(sourceFile, templateFile)) {
            return null;
        }

        ClassDesc[] classDescs = update(request.getPath(), request.getMethod(),
            className, templateFile);
        if (classDescs == null) {
            return null;
        }

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("classDescs", classDescs);
        return getSourceCreator().getResponseCreator().createResponse(
            "updateClasses", variableMap);
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
            classDesc = new ClassDesc(className);
            classDescriptorMap.put(className, classDesc);
        }

        ClassDesc[] classDescs = (ClassDesc[]) classDescriptorMap.values()
            .toArray(new ClassDesc[0]);
        if (classDescs.length == 0) {
            return null;
        }

        classDesc.setMethodDesc(new MethodDesc(getSourceCreator()
            .getActionName(path, method)));
        classDesc.setMethodDesc(new MethodDesc(
            DefaultRequestProcessor.ACTION_RENDER));

        for (int i = 0; i < classDescs.length; i++) {
            updateClass(classDescs[i], true);
        }

        return classDescs;
    }

    void updateClass(ClassDesc classDesc, boolean merge) {

        if (merge) {
            classDesc = mergeClassDescs(classDesc, getClassDesc(classDesc
                .getName()));
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

        return classDesc;
    }

    ClassDesc mergeClassDescs(ClassDesc classDesc1, ClassDesc classDesc2) {

        if (classDesc1 == null) {
            return classDesc2;
        } else if (classDesc2 == null) {
            return classDesc1;
        }
        PropertyDesc[] pd2s = classDesc2.getPropertyDescs();
        for (int i = 0; i < pd2s.length; i++) {
            PropertyDesc pd = classDesc1.getPropertyDesc(pd2s[i].getName());
            if (pd == null) {
                classDesc1.setPropertyDesc(pd2s[i]);
            } else {
                if (pd.getType() == null) {
                    pd.setType(pd2s[i].getType());
                }
                pd.addMode(pd2s[i].getMode());
            }
        }
        return classDesc1;
    }
}
