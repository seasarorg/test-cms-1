package org.seasar.cms.ymir.extension.creator.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.util.Properties;

import javax.servlet.ServletContext;

import org.seasar.cms.pluggable.Configuration;
import org.seasar.cms.pluggable.hotdeploy.LocalOndemandS2Container;
import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.MatchedPathMapping;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.RequestProcessor;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.ResponseCreator;
import org.seasar.cms.ymir.extension.Globals;
import org.seasar.cms.ymir.extension.creator.BodyDesc;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.ClassDescBag;
import org.seasar.cms.ymir.extension.creator.ClassDescSet;
import org.seasar.cms.ymir.extension.creator.DescValidator;
import org.seasar.cms.ymir.extension.creator.EntityMetaData;
import org.seasar.cms.ymir.extension.creator.MethodDesc;
import org.seasar.cms.ymir.extension.creator.ParameterDesc;
import org.seasar.cms.ymir.extension.creator.PathMetaData;
import org.seasar.cms.ymir.extension.creator.PropertyDesc;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.SourceGenerator;
import org.seasar.cms.ymir.extension.creator.TemplateAnalyzer;
import org.seasar.cms.ymir.extension.creator.TypeDesc;
import org.seasar.cms.ymir.extension.creator.action.Condition;
import org.seasar.cms.ymir.extension.creator.action.State;
import org.seasar.cms.ymir.extension.creator.action.UpdateAction;
import org.seasar.cms.ymir.extension.creator.action.UpdateActionSelector;
import org.seasar.cms.ymir.extension.creator.action.impl.CreateClassAction;
import org.seasar.cms.ymir.extension.creator.action.impl.CreateClassAndTemplateAction;
import org.seasar.cms.ymir.extension.creator.action.impl.CreateConfigurationAction;
import org.seasar.cms.ymir.extension.creator.action.impl.CreateTemplateAction;
import org.seasar.cms.ymir.extension.creator.action.impl.SystemConsoleAction;
import org.seasar.cms.ymir.extension.creator.action.impl.UpdateClassesAction;
import org.seasar.cms.ymir.impl.DefaultRequestProcessor;
import org.seasar.cms.ymir.impl.RedirectResponse;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.container.hotdeploy.OndemandProject;
import org.seasar.framework.container.hotdeploy.OndemandS2Container;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.log.Logger;

import net.skirnir.xom.IllegalSyntaxException;
import net.skirnir.xom.ValidationException;
import net.skirnir.xom.XMLParserFactory;
import net.skirnir.xom.XOMapper;
import net.skirnir.xom.XOMapperFactory;

public class SourceCreatorImpl implements SourceCreator {

    public static final String PARAM_PREFIX = "__ymir__";

    public static final String PARAM_TASK = PARAM_PREFIX + "task";

    private static final String SOURCECREATOR_PROPERTIES = "sourceCreator.properties";

    private Configuration configuration_;

    private DefaultRequestProcessor defaultRequestProcessor_;

    private LocalOndemandS2Container ondemandContainer_;

    private NamingConvention namingConvention_;

    private TemplateAnalyzer analyzer_;

    private String encoding_ = "UTF-8";

    private String rootPackageName_;

    private SourceGenerator sourceGenerator_;

    private ResponseCreator responseCreator_;

    public Properties sourceCreatorProperties_;

    private UpdateActionSelector actionSelector_ = new UpdateActionSelector()
            .register(
                    new Condition(State.ANY, State.ANY, State.FALSE,
                            Request.METHOD_GET), new CreateTemplateAction(this))
            .register(
                    new Condition(State.TRUE, State.ANY, State.TRUE,
                            Request.METHOD_GET), new UpdateClassesAction(this))
            .register(
                    new Condition(State.ANY, State.FALSE, State.FALSE,
                            Request.METHOD_POST),
                    new CreateClassAndTemplateAction(this))
            .register(
                    new Condition(State.TRUE, State.ANY, State.TRUE,
                            Request.METHOD_POST), new UpdateClassesAction(this))
            .register(
                    new Condition(State.TRUE, State.TRUE, State.FALSE,
                            Request.METHOD_POST),
                    new CreateTemplateAction(this)).register("createClass",
                    new CreateClassAction(this)).register("createTemplate",
                    new CreateTemplateAction(this)).register(
                    "createClassAndTemplate",
                    new CreateClassAndTemplateAction(this)).register(
                    "updateClasses", new UpdateClassesAction(this)).register(
                    "createConfiguration", new CreateConfigurationAction(this))
            .register("systemConsole", new SystemConsoleAction(this));

    public Logger logger_ = Logger.getLogger(getClass());

    public Response update(String path, String method, Request request) {

        PathMetaData pathMetaData = new LazyPathMetaData(this, path, method);
        String className = pathMetaData.getClassName();
        File sourceFile = pathMetaData.getSourceFile();
        File templateFile = pathMetaData.getTemplateFile();

        Object condition;

        if (!validateApplication(getApplication())) {
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
                condition = new Condition(State.valueOf(className != null),
                        State.valueOf(sourceFile.exists()), State
                                .valueOf(templateFile.exists()), method);
            }
        }

        UpdateAction action = actionSelector_.getAction(condition);
        if (action != null) {
            return action.act(request, pathMetaData);
        } else {
            return null;
        }
    }

    boolean validateApplication(Application application) {

        if (application.getProjectRoot() == null) {
            return false;
        } else if (!new File(application.getProjectRoot()).exists()) {
            return false;
        } else if (application.getRootPackageName() == null) {
            return false;
        }
        return true;
    }

    String getWelcomeFile() {

        XOMapper mapper = XOMapperFactory.newInstance();
        mapper.setStrict(false);
        File webXml = new File(getWebappRoot(), "WEB-INF/web.xml");
        if (!webXml.exists()) {
            return null;
        }

        WebApp webApp;
        try {
            webApp = (WebApp) mapper.toBean(XMLParserFactory.newInstance()
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

        Map<String, ClassDesc> classDescMap = new LinkedHashMap<String, ClassDesc>();
        for (int i = 0; i < pathMetaDatas.length; i++) {
            gatherClassDescs(classDescMap, pathMetaDatas[i]);
        }
        ClassDesc[] classDescs = addRelativeClassDescs(classDescMap.values()
                .toArray(new ClassDesc[0]));

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
                        || !ClassDesc.KIND_DTO.equals(td.getClassDesc()
                                .getKind())) {
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

    public void gatherClassDescs(Map<String, ClassDesc> classDescMap,
            PathMetaData pathMetaData) {

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
            if (getClassDesc(classDescs[i].getName(), false) == null) {
                classDescBag.addAsCreated(classDescs[i]);
            } else {
                classDescBag.addAsUpdated(classDescs[i]);
            }
        }

        return classDescBag;
    }

    public ClassDesc getClassDesc(String className,
            boolean onlyBaseInfoIfBaseExists) {

        Class clazz = getClass(className);
        if (clazz == null) {
            return null;
        }

        Class introspecedClass;
        if (onlyBaseInfoIfBaseExists) {
            introspecedClass = clazz.getSuperclass();
            if (introspecedClass == null) {
                introspecedClass = clazz;
            }
        } else {
            introspecedClass = clazz;
        }

        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(introspecedClass);
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
                logger_.info("**** PropertyType is NULL: name=" + name);
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

        Method[] methods = introspecedClass.getMethods();
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

        Map<String, List<ClassDesc>> pageByDtoMap = new HashMap<String, List<ClassDesc>>();
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
                List<ClassDesc> list = pageByDtoMap.get(cd.getName());
                if (list == null) {
                    list = new ArrayList<ClassDesc>();
                    pageByDtoMap.put(cd.getName(), list);
                }
                list.add(classDescs[i]);
            }
        }

        List<ClassDesc> classDescList = new ArrayList<ClassDesc>(Arrays
                .asList(classDescs));
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

        return classDescList.toArray(new ClassDesc[0]);
    }

    @SuppressWarnings("unchecked")
    void addSelectStatement(MethodDesc methodDesc, PropertyDesc propertyDesc,
            EntityMetaData metaData) {

        BodyDesc bodyDesc = methodDesc.getBodyDesc();
        Map<String, Object> root = new HashMap<String, Object>();
        root.put("entityMetaData", metaData);
        if (bodyDesc == null) {
            bodyDesc = new BodyDescImpl(DefaultRequestProcessor.ACTION_RENDER,
                    root);
        } else {
            bodyDesc.setRoot(root);
        }
        List<PropertyDesc> propertyDescList = (List<PropertyDesc>) root
                .get("propertyDescs");
        if (propertyDescList == null) {
            propertyDescList = new ArrayList<PropertyDesc>();
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
            classDescs[i].merge(getClassDesc(classDescs[i].getName(), true),
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
            // FIXME クラスローダの設定はs2-framework-2.4.0-beta-5以降では不要。
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(
                        ondemandContainer_.getContainer().getClassLoader());
                int size = ondemandContainer_.getProjectSize();
                for (int i = 0; i < size; i++) {
                    String className = ondemandContainer_.getProject(i)
                            .fromComponentNameToClassName(ondemandContainer_,
                                    componentName);
                    if (className != null) {
                        return className;
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
            return Class.forName(className, true, ondemandContainer_
                    .getContainer().getClassLoader());
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public File getTemplateFile(String path) {

        return new File(getWebappRoot(), path);
    }

    public File getSourceFile(String className) {

        return new File(getSourceDirectory(), className.replace('.', '/')
                + ".java");
    }

    File getClassFile(String className) {

        return new File(getClassesDirectory(), className.replace('.', '/')
                + ".class");
    }

    public Properties getSourceCreatorProperties() {

        if (sourceCreatorProperties_ == null) {
            sourceCreatorProperties_ = new Properties();
            File file = getSourceCreatorPropertiesFile();
            if (file.exists()) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    sourceCreatorProperties_.load(new BufferedInputStream(fis));
                } catch (IOException ex) {
                    logger_.error("Can't read properties: " + file);
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
            }
        }
        return sourceCreatorProperties_;
    }

    public File getSourceCreatorPropertiesFile() {

        return new File(getResourcesDirectory(), SOURCECREATOR_PROPERTIES);
    }

    public void saveSourceCreatorProperties() {

        if (sourceCreatorProperties_ == null) {
            return;
        }
        File file = getSourceCreatorPropertiesFile();
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            sourceCreatorProperties_.store(new BufferedOutputStream(fos), null);
        } catch (IOException ex) {
            logger_.error("Can't write properties: " + file);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public void setOndemandS2Container(OndemandS2Container container) {

        if (container instanceof LocalOndemandS2Container) {
            ondemandContainer_ = (LocalOndemandS2Container) container;
            if (rootPackageName_ == null) {
                if (ondemandContainer_.getProjectSize() > 0) {
                    OndemandProject project = ondemandContainer_.getProject(0);
                    rootPackageName_ = project.getRootPackageName();
                }
            }
        } else {
            throw new ComponentNotFoundRuntimeException(
                    "LocalOndemandS2Container");
        }
    }

    public void setNamingConvention(NamingConvention namingConvention) {

        namingConvention_ = namingConvention;
    }

    public void setRequestProcessor(RequestProcessor requestProcessor) {

        if (requestProcessor instanceof DefaultRequestProcessor) {
            defaultRequestProcessor_ = (DefaultRequestProcessor) requestProcessor;
        } else {
            throw new ComponentNotFoundRuntimeException(
                    "DefaultRequestProcessor");
        }
    }

    public File getSourceDirectory() {

        String sourceDirectory = getApplication().getSourceDirectory();
        if (sourceDirectory != null) {
            return new File(sourceDirectory);
        } else {
            return null;
        }
    }

    public File getClassesDirectory() {

        String classesDirectory = getApplication().getClassesDirectory();
        if (classesDirectory != null) {
            return new File(classesDirectory);
        } else {
            return null;
        }
    }

    public File getResourcesDirectory() {

        String resourcesDirectory = getApplication().getResourcesDirectory();
        if (resourcesDirectory != null) {
            return new File(resourcesDirectory);
        } else {
            return null;
        }
    }

    public File getWebappRoot() {

        return new File(getApplication().getWebappRoot());
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

    public String getRootPackageName() {

        return rootPackageName_;
    }

    public void setRootPackageName(String rootPackageName) {

        rootPackageName_ = rootPackageName;
    }

    public String getPagePackageName() {

        return rootPackageName_ + "." + namingConvention_.getWebPackageName();
    }

    public String getDtoPackageName() {

        return rootPackageName_ + "." + namingConvention_.getDtoPackageName();
    }

    public String getDaoPackageName() {

        return rootPackageName_ + "." + namingConvention_.getDaoPackageName();
    }

    public String getDxoPackageName() {

        return rootPackageName_ + "." + namingConvention_.getDxoPackageName();
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

    public Application getApplication() {
        return (Application) getServletContext().getAttribute(
                Globals.ATTR_APPLICATION);
    }

    ServletContext getServletContext() {
        return (ServletContext) getRootS2Container().getComponent(
                ServletContext.class);
    }

    S2Container getS2Container() {
        return getApplication().getS2Container();
    }

    S2Container getRootS2Container() {
        return SingletonS2ContainerFactory.getContainer();
    }

    public void registerUpdateAction(Object condition, UpdateAction updateAction) {

        actionSelector_.register(condition, updateAction);
    }
}
