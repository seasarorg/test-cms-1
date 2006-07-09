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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.seasar.cms.framework.Configuration;
import org.seasar.cms.framework.MatchedPathMapping;
import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.RequestProcessor;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.ResponseCreator;
import org.seasar.cms.framework.container.hotdeploy.LocalOndemandCreatorContainer;
import org.seasar.cms.framework.creator.BodyDesc;
import org.seasar.cms.framework.creator.ClassDesc;
import org.seasar.cms.framework.creator.ClassDescBag;
import org.seasar.cms.framework.creator.ClassDescSet;
import org.seasar.cms.framework.creator.DescValidator;
import org.seasar.cms.framework.creator.EntityMetaData;
import org.seasar.cms.framework.creator.MethodDesc;
import org.seasar.cms.framework.creator.ParameterDesc;
import org.seasar.cms.framework.creator.PathMetaData;
import org.seasar.cms.framework.creator.PropertyDesc;
import org.seasar.cms.framework.creator.SourceCreator;
import org.seasar.cms.framework.creator.SourceGenerator;
import org.seasar.cms.framework.creator.TemplateAnalyzer;
import org.seasar.cms.framework.creator.TypeDesc;
import org.seasar.cms.framework.creator.action.Condition;
import org.seasar.cms.framework.creator.action.UpdateAction;
import org.seasar.cms.framework.creator.action.UpdateActionSelector;
import org.seasar.cms.framework.creator.action.impl.CreateClassAction;
import org.seasar.cms.framework.creator.action.impl.CreateClassAndTemplateAction;
import org.seasar.cms.framework.creator.action.impl.CreateConfigurationAction;
import org.seasar.cms.framework.creator.action.impl.CreateTemplateAction;
import org.seasar.cms.framework.creator.action.impl.SystemConsoleAction;
import org.seasar.cms.framework.creator.action.impl.UpdateClassesAction;
import org.seasar.cms.framework.impl.DefaultRequestProcessor;
import org.seasar.cms.framework.impl.RedirectResponse;
import org.seasar.cms.framework.zpt.ZptResponseCreator;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.hotdeploy.OndemandCreatorContainer;
import org.seasar.framework.exception.ClassNotFoundRuntimeException;

import net.skirnir.xom.IllegalSyntaxException;
import net.skirnir.xom.ValidationException;
import net.skirnir.xom.XMLParserFactory;
import net.skirnir.xom.XOMapper;
import net.skirnir.xom.XOMapperFactory;

public class SourceCreatorImpl implements SourceCreator {

    public static final String PARAM_PREFIX = "__cms__";

    public static final String PARAM_TASK = PARAM_PREFIX + "task";

    private S2Container container_;

    private Configuration configuration_;

    private DefaultRequestProcessor defaultRequestProcessor_;

    private LocalOndemandCreatorContainer creatorContainer_;

    private File sourceDirectory_;

    private File classesDirectory_;

    private TemplateAnalyzer analyzer_;

    private String encoding_ = "UTF-8";

    private String pagePackageName_;

    private String dtoPackageName_;

    private String daoPackageName_;

    private String dxoPackageName_;

    private SourceGenerator sourceGenerator_;

    private ResponseCreator responseCreator_ = new ZptResponseCreator();

    private UpdateActionSelector actionSelector_ = new UpdateActionSelector()
        .register(new Condition(false, false, false, Request.METHOD_GET),
            new CreateTemplateAction(this)).register(
            new Condition(true, false, false, Request.METHOD_GET),
            new CreateTemplateAction(this)).register(
            new Condition(true, false, true, Request.METHOD_GET),
            new UpdateClassesAction(this)).register(
            new Condition(true, true, false, Request.METHOD_GET),
            new CreateTemplateAction(this)).register(
            new Condition(true, true, true, Request.METHOD_GET),
            new UpdateClassesAction(this)).register(
            new Condition(false, false, false, Request.METHOD_POST),
            new CreateClassAndTemplateAction(this)).register(
            new Condition(true, false, false, Request.METHOD_POST),
            new CreateClassAndTemplateAction(this)).register(
            new Condition(true, false, true, Request.METHOD_POST),
            new UpdateClassesAction(this)).register(
            new Condition(true, true, false, Request.METHOD_POST),
            new CreateTemplateAction(this)).register(
            new Condition(true, true, true, Request.METHOD_POST),
            new UpdateClassesAction(this)).register("createClass",
            new CreateClassAction(this)).register("createTemplate",
            new CreateTemplateAction(this)).register("createClassAndTemplate",
            new CreateClassAndTemplateAction(this)).register("updateClasses",
            new UpdateClassesAction(this)).register("createConfiguration",
            new CreateConfigurationAction(this)).register("systemConsole",
            new SystemConsoleAction(this));

    public Response update(String path, String method, Request request) {

        PathMetaData pathMetaData = new LazyPathMetaData(this, path, method);
        String className = pathMetaData.getClassName();
        File sourceFile = pathMetaData.getSourceFile();
        File templateFile = pathMetaData.getTemplateFile();

        Object condition;

        if (!validateConfiguration()) {
            condition = "createConfiguration";
        } else if (request.getParameter(PARAM_TASK) != null) {
            condition = request.getParameter(PARAM_TASK);
        } else {
            if ("".equals(path)) {
                String welcomeFile = getWelcomeFile();
                if (welcomeFile != null) {
                    return new RedirectResponse("/" + welcomeFile);
                }
                if (className == null || sourceFile.exists()) {
                    return null;
                }
                condition = "createClass";
            } else {
                condition = new Condition((className != null), sourceFile
                    .exists(), templateFile.exists(), method);
            }
        }

        UpdateAction action = actionSelector_.getAction(condition);
        if (action != null) {
            return action.act(request, pathMetaData);
        } else {
            return null;
        }
    }

    boolean validateConfiguration() {

        if (configuration_ == null) {
            return false;
        } else if (configuration_.getProperty(Configuration.KEY_PROJECTROOT) == null) {
            return false;
        } else if (!new File(configuration_
            .getProperty(Configuration.KEY_PROJECTROOT)).exists()) {
            return false;
        } else if (configuration_
            .getProperty(Configuration.KEY_ROOTPACKAGENAME) == null) {
            return false;
        }
        return true;
    }

    String getWelcomeFile() {

        XOMapper mapper = XOMapperFactory.newInstance();
        mapper.setStrict(false);
        File webXml = new File(getWebappDirectory(), "WEB-INF/web.xml");
        if (!webXml.exists()) {
            return null;
        }

        WebApp webApp;
        try {
            webApp = (WebApp) mapper.toBean(
                XMLParserFactory.newInstance()
                    .parse(
                        new InputStreamReader(new FileInputStream(webXml),
                            "UTF-8")).getRootElement(), WebApp.class);
        } catch (ValidationException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalSyntaxException ex) {
            throw new RuntimeException(ex);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        WelcomeFileList welcomeFileList = webApp.getWelcomeFileList();
        if (welcomeFileList == null) {
            return null;
        }
        String[] welcomeFiles = welcomeFileList.getWelcomeFiles();
        if (welcomeFiles.length > 0) {
            return welcomeFiles[0];
        } else {
            return null;
        }
    }

    public ClassDescBag gatherClassDescs(PathMetaData[] pathMetaDatas) {

        Map classDescMap = new LinkedHashMap();
        for (int i = 0; i < pathMetaDatas.length; i++) {
            gatherClassDescs(classDescMap, pathMetaDatas[i]);
        }
        ClassDesc[] classDescs = addRelativeClassDescs((ClassDesc[]) classDescMap
            .values().toArray(new ClassDesc[0]));

        return classifyClassDescs(classDescs);
    }

    public void updateClasses(ClassDescBag classDescBag, boolean mergeMethod) {

        ClassDescSet classDescSet = classDescBag.getClassDescSet();

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

                EntityMetaData metaData = new EntityMetaData(this, td
                    .getClassDesc().getName());
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
    }

    public void gatherClassDescs(Map classDescMap, PathMetaData pathMetaData) {

        String method = pathMetaData.getMethod();
        String className = pathMetaData.getClassName();
        try {
            analyzer_.analyze(method, classDescMap, new FileInputStream(
                pathMetaData.getTemplateFile()), encoding_, className);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        ClassDesc classDesc = (ClassDesc) classDescMap.get(className);
        if (classDesc == null && method.equalsIgnoreCase(Request.METHOD_POST)) {
            // テンプレートを解析した結果対応するPageクラスを作る必要があると
            // 見なされなかった場合でも、methodがPOSTならPageクラスを作る。
            classDesc = new ClassDescImpl(className);
            classDescMap.put(className, classDesc);
        }
        if (classDesc != null) {
            classDesc.setMethodDesc(new MethodDescImpl(getActionName(
                pathMetaData.getPath(), method)));
            MethodDesc methodDesc = new MethodDescImpl(
                DefaultRequestProcessor.ACTION_RENDER);
            classDesc.setMethodDesc(methodDesc);
        }
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

    public ClassDesc getClassDesc(String className) {

        Class clazz = getClass(className);
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
                EntityMetaData metaData = new EntityMetaData(this,
                    classDescs[i].getName());

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

    public boolean writeSourceFile(ClassDesc classDesc,
        ClassDescSet classDescSet) {

        if (!DescValidator.isValid(classDesc, classDescSet)) {
            return false;
        }

        writeString(sourceGenerator_.generateBaseSource(classDesc),
            getSourceFile(classDesc.getName() + "Base"));

        // gap側のクラスは存在しない場合のみ生成する。
        File sourceFile = getSourceFile(classDesc.getName());
        if (!sourceFile.exists()) {
            writeString(sourceGenerator_.generateGapSource(classDesc),
                sourceFile);
        }

        return true;
    }

    public void writeString(String string, File file) {

        if (string == null) {
            return;
        }

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

    public boolean isDenied(String path, String method) {

        if (path == null) {
            return true;
        }
        MatchedPathMapping matched = defaultRequestProcessor_
            .findMatchedPathMapping(path, method);
        if (matched == null) {
            return true;
        } else {
            return matched.getPathMapping().isDenied();
        }
    }

    public String getComponentName(String path, String method) {

        if (path == null) {
            return null;
        }
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

        if (path == null) {
            return null;
        }
        MatchedPathMapping matched = defaultRequestProcessor_
            .findMatchedPathMapping(path, method);
        if (matched == null) {
            return null;
        } else {
            return matched.getPathMapping().getActionName(
                matched.getVariableResolver());
        }
    }

    public String getDefaultPath(String path, String method) {

        if (path == null) {
            return null;
        }
        MatchedPathMapping matched = defaultRequestProcessor_
            .findMatchedPathMapping(path, method);
        if (matched == null) {
            return null;
        } else {
            return matched.getPathMapping().getDefaultPath(
                matched.getVariableResolver());
        }
    }

    public String getClassName(String componentName) {

        if (componentName == null) {
            return null;
        } else {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(
                    creatorContainer_.getClassLoader());
                int size = creatorContainer_.getCreatorSize();
                for (int i = 0; i < size; i++) {
                    try {
                        ComponentDef componentDef = creatorContainer_
                            .getCreator(i).getComponentDef(container_,
                                componentName);
                        if (componentDef != null) {
                            return componentDef.getComponentClass().getName();
                        }
                    } catch (ClassNotFoundRuntimeException ex) {
                        return ex.getCause().getMessage();
                    }
                }
            } finally {
                Thread.currentThread().setContextClassLoader(cl);
            }
        }
        return null;
    }

    public Class getClass(String className) {

        if (className == null) {
            return null;
        }
        try {
            return Class.forName(className, true, creatorContainer_
                .getClassLoader());
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public File getTemplateFile(String path) {

        return new File(getWebappDirectory(), path);
    }

    public File getSourceFile(String className) {

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

    public S2Container getContainer() {

        return container_;
    }

    public void setContainer(S2Container container) {

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

    public File getWebappDirectory() {

        return new File(configuration_
            .getProperty(Configuration.KEY_WEBAPPROOT));
    }

    public TemplateAnalyzer getTemplateAnalyzer() {

        return analyzer_;
    }

    public void setTemplateAnalyzer(TemplateAnalyzer analyzer) {

        analyzer_ = analyzer;
    }

    public String getEncoding() {

        return encoding_;
    }

    public void setEncoding(String encoding) {

        encoding_ = encoding;
    }

    public String getPagePackageName() {

        return pagePackageName_;
    }

    public void setPagePackageName(String pagePackageName) {

        pagePackageName_ = pagePackageName;
    }

    public String getDtoPackageName() {

        return dtoPackageName_;
    }

    public void setDtoPackageName(String dtoPackageName) {

        dtoPackageName_ = dtoPackageName;
    }

    public String getDaoPackageName() {

        return daoPackageName_;
    }

    public void setDaoPackageName(String daoPackageName) {

        daoPackageName_ = daoPackageName;
    }

    public String getDxoPackageName() {

        return dxoPackageName_;
    }

    public void setDxoPackageName(String dxoPackageName) {

        dxoPackageName_ = dxoPackageName;
    }

    public SourceGenerator getSourceGenerator() {

        return sourceGenerator_;
    }

    public void setSourceGenerator(SourceGenerator sourceGenerator) {

        sourceGenerator_ = sourceGenerator;
    }

    public ResponseCreator getResponseCreator() {

        return responseCreator_;
    }

    public void setResponseCreator(ResponseCreator responseCreator) {

        responseCreator_ = responseCreator;
    }

    public Configuration getConfiguration() {

        return configuration_;
    }

    public void setConfiguration(Configuration configuration) {

        configuration_ = configuration;
    }

    public void registerUpdateAction(Object condition, UpdateAction updateAction) {

        actionSelector_.register(condition, updateAction);
    }
}