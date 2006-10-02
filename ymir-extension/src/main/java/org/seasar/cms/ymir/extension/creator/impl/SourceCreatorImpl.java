package org.seasar.cms.ymir.extension.creator.impl;

import static org.seasar.cms.ymir.extension.Globals.APPKEYPREFIX_SOURCECREATOR_SUPERCLASS;
import static org.seasar.cms.ymir.extension.Globals.APPKEY_SOURCECREATOR_ENABLE;
import static org.seasar.cms.ymir.extension.Globals.APPKEY_SOURCECREATOR_SUPERCLASS;
import static org.seasar.cms.ymir.impl.DefaultRequestProcessor.PARAM_METHOD;

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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.seasar.cms.pluggable.hotdeploy.LocalOndemandS2Container;
import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.ApplicationManager;
import org.seasar.cms.ymir.MatchedPathMapping;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.RequestProcessor;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.ResponseCreator;
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
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
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

    private DefaultRequestProcessor defaultRequestProcessor_;

    private NamingConvention namingConvention_;

    private TemplateAnalyzer analyzer_;

    private String encoding_ = "UTF-8";

    private SourceGenerator sourceGenerator_;

    private ResponseCreator responseCreator_;

    public Properties sourceCreatorProperties_;

    private ApplicationManager applicationManager_;

    private UpdateActionSelector actionSelector_ = new UpdateActionSelector()
            .register(
                    new Condition(State.ANY, State.ANY, State.FALSE,
                            Request.METHOD_GET), new CreateTemplateAction(this))
            .register(
                    new Condition(State.ANY, State.ANY, State.TRUE,
                            Request.METHOD_GET), new UpdateClassesAction(this))
            .register(
                    new Condition(State.ANY, State.FALSE, State.FALSE,
                            Request.METHOD_POST),
                    new CreateClassAndTemplateAction(this))
            .register(
                    new Condition(State.ANY, State.ANY, State.TRUE,
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

    public Response update(Request request, Response response) {

        Application application = getApplication();
        if (!shouldUpdate(application)) {
            return response;
        }

        String path = request.getPath();
        String method = getOriginalMethod(request);
        String forwardPath = null;
        if (response.getType() == Response.TYPE_FORWARD) {
            forwardPath = response.getPath();
        } else if (response.getType() == Response.TYPE_PASSTHROUGH) {
            forwardPath = path;
        }
        PathMetaData pathMetaData = new LazyPathMetaData(this, path, method,
                forwardPath);

        Object condition;

        if (!isAlreadyConfigured(application)) {
            condition = "createConfiguration";
        } else if (request.getParameter(PARAM_TASK) != null) {
            condition = request.getParameter(PARAM_TASK);
        } else {
            String className = pathMetaData.getClassName();
            File sourceFile = pathMetaData.getSourceFile();
            File templateFile = pathMetaData.getTemplateFile();

            if ("".equals(forwardPath)) {
                String welcomeFile = getWelcomeFile();
                if (welcomeFile != null) {
                    return response;
                }
                if (className == null || sourceFile.exists()) {
                    return response;
                }
                condition = "createClass";
            } else {
                condition = new Condition(State.valueOf(className != null),
                        State.valueOf(sourceFile.exists()), State
                                .valueOf(templateFile != null ? templateFile
                                        .exists() : true), method);
            }
        }

        UpdateAction action = actionSelector_.getAction(condition);
        if (action != null) {
            Response newResponse = action.act(request, pathMetaData);
            if (newResponse != null) {
                response = newResponse;
            }
        }
        return response;
    }

    String getOriginalMethod(Request request) {

        String originalMethod = request.getParameter(PARAM_METHOD);
        if (originalMethod != null) {
            return originalMethod;
        } else {
            return request.getMethod();
        }
    }

    boolean shouldUpdate(Application application) {

        return (!"false".equals(application
                .getProperty(APPKEY_SOURCECREATOR_ENABLE)));
    }

    boolean isAlreadyConfigured(Application application) {

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
        File webXml = new File(getWebappSourceRoot(), "WEB-INF/web.xml");
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

        String path = pathMetaData.getPath();
        String method = pathMetaData.getMethod();
        String className = pathMetaData.getClassName();
        try {
            analyzer_.analyze(path, method, classDescMap, new FileInputStream(
                    pathMetaData.getTemplateFile()), encoding_, className);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        ClassDesc classDesc = classDescMap.get(className);
        if (classDesc == null && method.equalsIgnoreCase(Request.METHOD_POST)) {
            // テンプレートを解析した結果対応するPageクラスを作る必要があると
            // 見なされなかった場合でも、methodがPOSTならPageクラスを作る。
            classDesc = newClassDesc(className);
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
            if (getClass(classDescs[i].getName()) == null) {
                classDescBag.addAsCreated(classDescs[i]);
            } else {
                classDescBag.addAsUpdated(classDescs[i]);
            }
        }

        return classDescBag;
    }

    public ClassDesc getClassDesc(Class clazz, String className) {

        if (clazz == null) {
            return null;
        }

        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }

        ClassDesc classDesc = newClassDesc(className);

        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < pds.length; i++) {
            String name = pds[i].getName();
            Method readMethod = pds[i].getReadMethod();
            Method writeMethod = pds[i].getWriteMethod();
            // このクラスで定義されているプロパティだけを対象とする。
            if (readMethod != null && readMethod.getDeclaringClass() != clazz) {
                readMethod = null;
            }
            if (writeMethod != null && writeMethod.getDeclaringClass() != clazz) {
                writeMethod = null;
            }

            PropertyDesc propertyDesc = new PropertyDescImpl(name);
            int mode = PropertyDesc.NONE;
            if (readMethod != null) {
                mode |= PropertyDesc.READ;
            }
            if (writeMethod != null) {
                mode |= PropertyDesc.WRITE;
            }
            if (mode == PropertyDesc.NONE) {
                continue;
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

        // このクラスで定義されているメソッドだけを対象とする。
        Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            String name = methods[i].getName();
            if (name.startsWith("get") || name.startsWith("is")
                    || name.startsWith("set")) {
                continue;
            }
            if (methods[i].getDeclaringClass() == Object.class) {
                continue;
            }
            classDesc.setMethodDesc(new MethodDescImpl(methods[i]));
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
            mergeWithExistentClass(classDescs[i], mergeMethod);
            if (!writeSourceFile(classDescs[i], classDescBag.getClassDescSet())) {
                // ソースファイルの生成に失敗した。
                classDescBag.remove(classDescs[i].getName());
                classDescBag.addAsFailed(classDescs[i]);

            }
        }
    }

    public void mergeWithExistentClass(ClassDesc desc, boolean mergeMethod) {

        String className = desc.getName();
        Class clazz = getClass(className);
        ClassDesc gapDesc = getClassDesc(clazz, className);
        if (gapDesc == null) {
            gapDesc = newClassDesc(className);
        }
        Class baseClass = (clazz != null ? clazz.getSuperclass() : null);
        ClassDesc baseDesc = (mergeMethod ? getClassDesc(baseClass, className)
                : null);
        if (baseDesc == null) {
            baseDesc = newClassDesc(className);
        }
        ClassDesc superDesc = getClassDesc(getClass(desc.getSuperclassName()),
                className);
        if (superDesc == null) {
            superDesc = newClassDesc(className);
        }

        // baseにあるものは必ず残す。baseになくてsuperやgapにあるものは除去する。
        ClassDesc generated = (ClassDesc) desc.clone();
        desc.clear();
        desc.merge(baseDesc);
        PropertyDesc[] pds = generated.getPropertyDescs();
        for (int i = 0; i < pds.length; i++) {
            PropertyDesc basePd = baseDesc.getPropertyDesc(pds[i].getName());
            if (basePd == null || !basePd.isReadable()) {
                removeModeFrom(pds[i], PropertyDesc.READ, gapDesc);
                removeModeFrom(pds[i], PropertyDesc.READ, superDesc);
            }
            if (basePd == null || !basePd.isWritable()) {
                removeModeFrom(pds[i], PropertyDesc.WRITE, gapDesc);
                removeModeFrom(pds[i], PropertyDesc.WRITE, superDesc);
            }
            if (!pds[i].isReadable() && !pds[i].isWritable()) {
                generated.removePropertyDesc(pds[i].getName());
            }
        }
        MethodDesc[] mds = generated.getMethodDescs();
        for (int i = 0; i < mds.length; i++) {
            if (baseDesc.getMethodDesc(mds[i]) == null
                    && (gapDesc.getMethodDesc(mds[i]) != null || superDesc
                            .getMethodDesc(mds[i]) != null)) {
                generated.removeMethodDesc(mds[i]);
            }
        }

        desc.merge(generated);
    }

    void removeModeFrom(PropertyDesc pd, int mode, ClassDesc cd) {

        PropertyDesc p = cd.getPropertyDesc(pd.getName());
        if (p != null) {
            if ((p.getMode() & mode) != 0) {
                pd.setMode(pd.getMode() & ~mode);
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

    public MatchedPathMapping findMatchedPathMapping(String path, String method) {
        if (path == null) {
            return null;
        }
        return defaultRequestProcessor_.findMatchedPathMapping(path, method);
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

    public String getClassName(String componentName) {

        if (componentName == null) {
            return null;
        } else {
            LocalOndemandS2Container ondemandContainer = getApplication()
                    .getOndemandS2Container();
            int size = ondemandContainer.getProjectSize();
            for (int i = 0; i < size; i++) {
                String className = ondemandContainer.getProject(i)
                        .fromComponentNameToClassName(ondemandContainer,
                                componentName);
                if (className != null) {
                    return className;
                }
            }
        }
        return null;
    }

    public Class getClass(String className) {

        if (className == null) {
            return null;
        }
        try {
            return Class.forName(className, true, getS2Container()
                    .getClassLoader());
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public File getTemplateFile(String path) {

        return new File(getWebappSourceRoot(), path);
    }

    public File getSourceFile(String className) {

        return new File(getSourceDirectory(), className.replace('.', '/')
                + ".java");
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

    public File getResourcesDirectory() {

        String resourcesDirectory = getApplication().getResourcesDirectory();
        if (resourcesDirectory != null) {
            return new File(resourcesDirectory);
        } else {
            return null;
        }
    }

    public File getWebappSourceRoot() {

        return new File(getApplication().getWebappSourceRoot());
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

        return getApplication().getRootPackageName();
    }

    public String getPagePackageName() {

        return getRootPackageName() + "."
                + namingConvention_.getWebPackageName();
    }

    public String getDtoPackageName() {

        return getRootPackageName() + "."
                + namingConvention_.getDtoPackageName();
    }

    public String getDaoPackageName() {

        return getRootPackageName() + "."
                + namingConvention_.getDaoPackageName();
    }

    public String getDxoPackageName() {

        return getRootPackageName() + "."
                + namingConvention_.getDxoPackageName();
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

    public Application getApplication() {

        return applicationManager_.getContextApplication();
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

    public void setApplicationManager(ApplicationManager applicationManager) {

        applicationManager_ = applicationManager;
    }

    public ClassDesc newClassDesc(String className) {

        ClassDescImpl classDescImpl = new ClassDescImpl(className);

        // スーパークラスをセットする。
        Application application = getApplication();
        for (Enumeration enm = application.propertyNames(); enm
                .hasMoreElements();) {
            String key = (String) enm.nextElement();
            if (!key.startsWith(APPKEYPREFIX_SOURCECREATOR_SUPERCLASS)) {
                continue;
            }
            if (!Pattern.compile(
                    key.substring(APPKEYPREFIX_SOURCECREATOR_SUPERCLASS
                            .length())).matcher(className).find()) {
                continue;
            }
            String superclassName = application.getProperty(key);
            Class superclass = getClass(superclassName);
            if (superclass == null) {
                throw new RuntimeException(
                        "Superclass is not found: superclass key=" + key
                                + ", value=" + superclassName);
            }
            classDescImpl.setSuperclass(superclass);
            break;
        }
        if (classDescImpl.getSuperclassName() == null) {
            String superclassName = application
                    .getProperty(APPKEY_SOURCECREATOR_SUPERCLASS);
            Class superclass = getClass(superclassName);
            if (superclassName != null && superclass == null) {
                throw new RuntimeException(
                        "Superclass is not found: superclass key="
                                + APPKEY_SOURCECREATOR_SUPERCLASS + ", value="
                                + superclassName);
            }
            classDescImpl.setSuperclass(superclass);
        }

        return classDescImpl;
    }
}
