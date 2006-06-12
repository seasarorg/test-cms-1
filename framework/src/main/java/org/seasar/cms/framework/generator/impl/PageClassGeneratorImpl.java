package org.seasar.cms.framework.generator.impl;

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
import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.framework.MatchedPathMapping;
import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.RequestProcessor;
import org.seasar.cms.framework.container.hotdeploy.LocalOndemandCreatorContainer;
import org.seasar.cms.framework.generator.ClassDesc;
import org.seasar.cms.framework.generator.JavaSourceGenerator;
import org.seasar.cms.framework.generator.PageClassGenerator;
import org.seasar.cms.framework.generator.PropertyDesc;
import org.seasar.cms.framework.generator.TemplateAnalyzer;
import org.seasar.cms.framework.impl.DefaultRequestProcessor;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.hotdeploy.OndemandCreatorContainer;
import org.seasar.framework.exception.ClassNotFoundRuntimeException;

public class PageClassGeneratorImpl implements PageClassGenerator {

    private S2Container container_;

    private DefaultRequestProcessor defaultRequestProcessor_;

    private LocalOndemandCreatorContainer creatorContainer_;

    private File sourceDirectory_;

    private File classesDirectory_;

    private File webappDirectory_;

    private TemplateAnalyzer analyzer_;

    private String encoding_ = "UTF-8";

    private String dtoPackageName_;

    private JavaSourceGenerator javaSourceGenerator_;

    public ClassDesc[] update(String path) {

        String className = getClassName(getComponentName(path));
        if (className == null) {
            return null;
        }

        File templateFile = getTemplateFile(path);
        if (!templateFile.exists()) {
            return null;
        }

        File classFile = getClassFile(className + "Base");
        if (classFile.exists()
            && classFile.lastModified() >= templateFile.lastModified()) {
            return null;
        }

        Map classDescriptorMap = new HashMap();
        try {
            analyzer_.analyze(classDescriptorMap, new FileInputStream(
                templateFile), encoding_, className);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        ClassDesc[] descriptors = (ClassDesc[]) classDescriptorMap.values()
            .toArray(new ClassDesc[0]);

        for (int i = 0; i < descriptors.length; i++) {
            updateClass(descriptors[i], true);
        }

        return descriptors;
    }

    public String getComponentName(String path) {

        MatchedPathMapping matched = defaultRequestProcessor_
            .findMatchedPathMapping(path, Request.METHOD_GET);
        if (matched == null) {
            return null;
        } else {
            return matched.getPathMapping().getComponentName(
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
        writePageSourceFile(classDesc);
    }

    void writePageSourceFile(ClassDesc classDesc) {

        writeString(javaSourceGenerator_.generatePageBaseSource(classDesc),
            getSourceFile(classDesc.getName() + "Base"));

        // gap側のクラスは存在しない場合のみ生成する。
        File sourceFile = getSourceFile(classDesc.getName());
        if (!sourceFile.exists()) {
            writeString(javaSourceGenerator_.generatePageSource(classDesc),
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
                pd.addMode(pd2s[i].getMode() & ~PropertyDesc.ARRAY);
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
            if (propertyType.isArray()) {
                mode |= PropertyDesc.ARRAY;
                propertyType = propertyType.getComponentType();
            }
            propertyDesc.setType(propertyType.getName());
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

    public void setJavaSourceGenerator(JavaSourceGenerator javaSourceGenerator) {

        javaSourceGenerator_ = javaSourceGenerator;
    }
}
