package org.seasar.cms.framework.creator.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.seasar.cms.framework.MatchedPathMapping;
import org.seasar.cms.framework.RequestProcessor;
import org.seasar.cms.framework.container.hotdeploy.LocalOndemandCreatorContainer;
import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.MethodDesc;
import org.seasar.cms.framework.creator.PropertyDesc;
import org.seasar.cms.framework.creator.SourceCreator;
import org.seasar.cms.framework.creator.SourceGenerator;
import org.seasar.cms.framework.creator.TemplateAnalyzer;
import org.seasar.cms.framework.impl.DefaultRequestProcessor;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.hotdeploy.OndemandCreatorContainer;
import org.seasar.framework.exception.ClassNotFoundRuntimeException;

public class SourceCreatorImpl implements SourceCreator {

    private S2Container container_;

    private DefaultRequestProcessor defaultRequestProcessor_;

    private LocalOndemandCreatorContainer creatorContainer_;

    private File sourceDirectory_;

    private File classesDirectory_;

    private File webappDirectory_;

    private TemplateAnalyzer analyzer_;

    private String encoding_ = "UTF-8";

    private String dtoPackageName_;

    private SourceGenerator javaSourceGenerator_;

    public ClassDesc[] update(String path, String method) {

        String className = getClassName(getComponentName(path, method));
        if (className == null) {
            return null;
        }

        File templateFile = getTemplateFile(path);
        if (!templateFile.exists()) {
            return null;
        }

        File sourceFile = getSourceFile(className + "Base");
        if (sourceFile.exists()
            && sourceFile.lastModified() >= templateFile.lastModified()) {
            return null;
        }

        Map classDescriptorMap = new LinkedHashMap();
        try {
            analyzer_.analyze(method, classDescriptorMap, new FileInputStream(
                templateFile), encoding_, className);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        ClassDesc[] classDescs = (ClassDesc[]) classDescriptorMap.values()
            .toArray(new ClassDesc[0]);
        if (classDescs.length == 0) {
            return null;
        }

        ClassDesc classDesc = (ClassDesc) classDescriptorMap.get(className);
        classDesc.setMethodDesc(new MethodDesc(getActionName(path, method)));
        classDesc.setMethodDesc(new MethodDesc(
            DefaultRequestProcessor.ACTION_RENDER));

        for (int i = 0; i < classDescs.length; i++) {
            updateClass(classDescs[i], true);
        }

        return classDescs;
    }

    public String getComponentName(String path, String method) {

        MatchedPathMapping matched = defaultRequestProcessor_
            .findMatchedPathMapping(path, method);
        if (matched == null) {
            return null;
        } else {
            return matched.getPathMapping().getComponentName(
                matched.getVariableResolver());
        }
    }

    public String getActionName(String path, String method) {

        MatchedPathMapping matched = defaultRequestProcessor_
            .findMatchedPathMapping(path, method);
        if (matched == null) {
            return null;
        } else {
            return matched.getPathMapping().getActionName(
                matched.getVariableResolver());
        }
    }

    public String getClassName(String componentName) {

        if (componentName == null) {
            return null;
        } else if (container_.hasComponentDef(componentName)) {
            return container_.getComponentDef(componentName)
                .getComponentClass().getName();
        } else {
            int size = creatorContainer_.getCreatorSize();
            for (int i = 0; i < size; i++) {
                try {
                    creatorContainer_.getCreator(i).getComponentDef(container_,
                        componentName);
                } catch (ClassNotFoundRuntimeException ex) {
                    return ex.getCause().getMessage();
                }
            }
        }
        return null;
    }

    void updateClass(ClassDesc classDesc, boolean merge) {

        if (merge) {
            classDesc = mergeClassDescs(classDesc, getClassDesc(classDesc
                .getName()));
        }
        writeSourceFile(classDesc);
    }

    void writeSourceFile(ClassDesc classDesc) {

        writeString(javaSourceGenerator_.generateBaseSource(classDesc),
            getSourceFile(classDesc.getName() + "Base"));

        // gap側のクラスは存在しない場合のみ生成する。
        File sourceFile = getSourceFile(classDesc.getName());
        if (!sourceFile.exists()) {
            writeString(javaSourceGenerator_.generateGapSource(classDesc),
                sourceFile);
        }
    }

    void writeString(String string, File file) {

        file.getParentFile().mkdirs();

        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                os, encoding_));
            writer.write(string);
            writer.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ignore) {
                }
            }
        }
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

    ClassDesc getClassDesc(String className) {

        Class clazz = null;
        try {
            clazz = Class.forName(className, true, container_.getClassLoader());
        } catch (ClassNotFoundException ex) {
            return null;
        }

        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }

        ClassDesc classDesc = new ClassDesc(className);
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

    File getTemplateFile(String path) {

        return new File(webappDirectory_, path);
    }

    File getSourceFile(String className) {

        return new File(sourceDirectory_, className.replace('.', '/') + ".java");
    }

    File getClassFile(String className) {

        return new File(classesDirectory_, className.replace('.', '/')
            + ".class");
    }

    public void setOndemandCreatorContainer(OndemandCreatorContainer container) {

        if (container instanceof LocalOndemandCreatorContainer) {
            creatorContainer_ = (LocalOndemandCreatorContainer) container;
        } else {
            throw new ComponentNotFoundRuntimeException(
                "LocalOndemandCreatorContainer");
        }
    }

    public void setS2Container(S2Container container) {

        container_ = container;
    }

    public void setRequestProcessor(RequestProcessor requestProcessor) {

        if (requestProcessor instanceof DefaultRequestProcessor) {
            defaultRequestProcessor_ = (DefaultRequestProcessor) requestProcessor;
        } else {
            throw new ComponentNotFoundRuntimeException(
                "DefaultRequestProcessor");
        }
    }

    public void setSourceDirectoryPath(String sourceDirectoryPath) {

        sourceDirectory_ = new File(sourceDirectoryPath);
    }

    public void setClassesDirectoryPath(String classesDirectoryPath) {

        classesDirectory_ = new File(classesDirectoryPath);
    }

    public void setWebappDirectoryPath(String webappDirectoryPath) {

        webappDirectory_ = new File(webappDirectoryPath);
    }

    public void setTemplateAnalyzer(TemplateAnalyzer analyzer) {

        analyzer_ = analyzer;
    }

    public void setEncoding(String encoding) {

        encoding_ = encoding;
    }

    public String getDtoPackageName() {

        return dtoPackageName_;
    }

    public void setDtoPackageName(String dtoPackageName) {

        dtoPackageName_ = dtoPackageName;
    }

    public void setSourceGenerator(SourceGenerator javaSourceGenerator) {

        javaSourceGenerator_ = javaSourceGenerator;
    }
}
