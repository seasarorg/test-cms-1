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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
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
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.cms.pluggable.hotdeploy.LocalHotdeployS2Container;
import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.ApplicationManager;
import org.seasar.cms.ymir.MatchedPathMapping;
import org.seasar.cms.ymir.MessageNotFoundRuntimeException;
import org.seasar.cms.ymir.MessagesNotFoundRuntimeException;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.RequestProcessor;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.ResponseCreator;
import org.seasar.cms.ymir.WrappingRuntimeException;
import org.seasar.cms.ymir.extension.creator.AnnotationDesc;
import org.seasar.cms.ymir.extension.creator.BodyDesc;
import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.ClassDescBag;
import org.seasar.cms.ymir.extension.creator.ClassDescSet;
import org.seasar.cms.ymir.extension.creator.DescValidator;
import org.seasar.cms.ymir.extension.creator.EntityMetaData;
import org.seasar.cms.ymir.extension.creator.InvalidClassDescException;
import org.seasar.cms.ymir.extension.creator.MethodDesc;
import org.seasar.cms.ymir.extension.creator.ParameterDesc;
import org.seasar.cms.ymir.extension.creator.PathMetaData;
import org.seasar.cms.ymir.extension.creator.PropertyDesc;
import org.seasar.cms.ymir.extension.creator.PropertyTypeHintBag;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.SourceGenerator;
import org.seasar.cms.ymir.extension.creator.Template;
import org.seasar.cms.ymir.extension.creator.TemplateAnalyzer;
import org.seasar.cms.ymir.extension.creator.TemplateProvider;
import org.seasar.cms.ymir.extension.creator.TypeDesc;
import org.seasar.cms.ymir.extension.creator.DescValidator.Result;
import org.seasar.cms.ymir.extension.creator.action.ActionSelector;
import org.seasar.cms.ymir.extension.creator.action.Condition;
import org.seasar.cms.ymir.extension.creator.action.State;
import org.seasar.cms.ymir.extension.creator.action.UpdateAction;
import org.seasar.cms.ymir.extension.creator.action.UpdateByExceptionAction;
import org.seasar.cms.ymir.extension.creator.action.impl.CreateClassAction;
import org.seasar.cms.ymir.extension.creator.action.impl.CreateClassAndTemplateAction;
import org.seasar.cms.ymir.extension.creator.action.impl.CreateConfigurationAction;
import org.seasar.cms.ymir.extension.creator.action.impl.CreateMessageAction;
import org.seasar.cms.ymir.extension.creator.action.impl.CreateMessagesAction;
import org.seasar.cms.ymir.extension.creator.action.impl.CreateTemplateAction;
import org.seasar.cms.ymir.extension.creator.action.impl.DoEditTemplateAction;
import org.seasar.cms.ymir.extension.creator.action.impl.DoUpdateTemplateAction;
import org.seasar.cms.ymir.extension.creator.action.impl.ResourceAction;
import org.seasar.cms.ymir.extension.creator.action.impl.SystemConsoleAction;
import org.seasar.cms.ymir.extension.creator.action.impl.UpdateClassesAction;
import org.seasar.cms.ymir.impl.DefaultRequestProcessor;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.mock.servlet.MockHttpServletRequestImpl;
import org.seasar.framework.mock.servlet.MockHttpServletResponseImpl;
import org.seasar.framework.mock.servlet.MockServletContextImpl;
import org.seasar.kvasir.util.StringUtils;
import org.seasar.kvasir.util.collection.MapProperties;

import net.skirnir.freyja.EvaluationRuntimeException;

public class SourceCreatorImpl implements SourceCreator {

    private static final String SOURCECREATOR_PROPERTIES = "sourceCreator.properties";

    public static final ServletContext MOCK_SERVLETCONTEXT = new MockServletContextImpl(
            "/") {
        private static final long serialVersionUID = 0;

        public String getServletContextName() {
            return "";
        }
    };

    public static final HttpServletRequest MOCK_REQUEST = new MockHttpServletRequestImpl(
            MOCK_SERVLETCONTEXT, "/");

    public static final HttpServletResponse MOCK_RESPONSE = new MockHttpServletResponseImpl(
            MOCK_REQUEST);

    private DefaultRequestProcessor defaultRequestProcessor_;

    private NamingConvention namingConvention_;

    private TemplateAnalyzer analyzer_;

    private String encoding_ = "UTF-8";

    private SourceGenerator sourceGenerator_;

    private ResponseCreator responseCreator_;

    public Properties sourceCreatorProperties_;

    private ApplicationManager applicationManager_;

    private TemplateProvider templateProvider_ = new DefaultTemplateProvider(
            this);

    private ActionSelector<UpdateAction> actionSelector_ = new ActionSelector<UpdateAction>()
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
            .register("systemConsole", new SystemConsoleAction(this)).register(
                    "resource", new ResourceAction(this)).register(
                    "editTemplate.do", new DoEditTemplateAction(this))
            .register("updateTemplate.do", new DoUpdateTemplateAction(this));

    private ActionSelector<UpdateByExceptionAction> byExceptionActionSelector_ = new ActionSelector<UpdateByExceptionAction>()
            .register(MessagesNotFoundRuntimeException.class,
                    new CreateMessagesAction(this)).register("createMessages",
                    new CreateMessagesAction(this)).register(
                    MessageNotFoundRuntimeException.class,
                    new CreateMessageAction(this)).register("createMessage",
                    new CreateMessageAction(this));

    public Log log_ = LogFactory.getLog(SourceCreatorImpl.class);

    public Response update(Request request, Response response) {

        Application application = getApplication();
        String path = request.getPath();
        Object condition = null;
        if (request.getParameter(PARAM_TASK) != null) {
            condition = request.getParameter(PARAM_TASK);
        } else if (path.startsWith(PATH_PREFIX)) {
            int slash = path.indexOf('/', PATH_PREFIX.length());
            if (slash >= 0) {
                condition = path.substring(PATH_PREFIX.length(), slash);
            } else {
                condition = path.substring(PATH_PREFIX.length());
            }
        } else {
            if (!request.isMatched()) {
                return response;
            }

            if (!shouldUpdate(application)) {
                return response;
            }

            if (response.getType() != Response.TYPE_PASSTHROUGH
                    && response.getType() != Response.TYPE_FORWARD) {
                return response;
            }
        }

        String method = getOriginalMethod(request);
        String forwardPath = null;
        if (response.getType() == Response.TYPE_FORWARD) {
            forwardPath = response.getPath();
        } else if (response.getType() == Response.TYPE_PASSTHROUGH) {
            forwardPath = path;
        }
        PathMetaData pathMetaData = new LazyPathMetaData(this, path, method,
                forwardPath);

        if (condition == null) {
            if (!isAlreadyConfigured(application)) {
                condition = "createConfiguration";
            } else {
                String className = pathMetaData.getClassName();
                File sourceFile = pathMetaData.getSourceFile();
                Template template = pathMetaData.getTemplate();

                if ("".equals(forwardPath)) {
                    if (className == null || sourceFile.exists()) {
                        return response;
                    }
                    condition = "createClass";
                } else {
                    condition = new Condition(State.valueOf(className != null),
                            State.valueOf(sourceFile.exists()), State
                                    .valueOf(template.exists()), method);
                }
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

    public Response updateByException(Request request, Throwable t) {

        if ((t instanceof WrappingRuntimeException || t instanceof EvaluationRuntimeException)
                && t.getCause() != null) {
            t = t.getCause();
        }
        String path = request.getPath();
        Object condition = null;
        if (request.getParameter(PARAM_TASK) != null) {
            condition = request.getParameter(PARAM_TASK);
        } else if (path.startsWith(PATH_PREFIX)) {
            int slash = path.indexOf('/', PATH_PREFIX.length());
            if (slash >= 0) {
                condition = path.substring(PATH_PREFIX.length(), slash);
            } else {
                condition = path.substring(PATH_PREFIX.length());
            }
        } else {
            condition = t.getClass();
        }

        UpdateByExceptionAction action = byExceptionActionSelector_
                .getAction(condition);
        if (action == null) {
            return null;
        } else {
            return action.act(request, t);
        }
    }

    String getOriginalMethod(Request request) {

        String originalMethod = request.getParameter(PARAM_METHOD);
        if (originalMethod != null) {
            return originalMethod;
        } else {
            return request.getMethod();
        }
    }

    public boolean shouldUpdate(Application application) {

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

    public ClassDescBag gatherClassDescs(PathMetaData[] pathMetaDatas) {
        return gatherClassDescs(pathMetaDatas, null, null);
    }

    public ClassDescBag gatherClassDescs(PathMetaData[] pathMetaDatas,
            PropertyTypeHintBag hintBag, String[] ignoreVariables) {

        Map<String, ClassDesc> classDescMap = new LinkedHashMap<String, ClassDesc>();
        for (int i = 0; i < pathMetaDatas.length; i++) {
            gatherClassDescs(classDescMap, pathMetaDatas[i], hintBag,
                    ignoreVariables);
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
                if (!DescValidator.validate(td, classDescSet).isValid()
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
            PathMetaData pathMetaData, PropertyTypeHintBag hintBag,
            String[] ignoreVariables) {

        String path = pathMetaData.getPath();
        String method = pathMetaData.getMethod();
        String className = pathMetaData.getClassName();
        analyzer_
                .analyze(getServletContext(), getHttpServletRequest(),
                        getHttpServletResponse(), path, method, classDescMap,
                        pathMetaData.getTemplate(), className, hintBag,
                        ignoreVariables);

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
            classDesc.setMethodDesc(new MethodDescImpl(
                    DefaultRequestProcessor.ACTION_RENDER));
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

        AnnotationDesc[] ads = createAnnotationDescs(clazz);
        for (int i = 0; i < ads.length; i++) {
            classDesc.setAnnotationDesc(ads[i]);
        }

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
                log_.info("**** PropertyType is NULL: name=" + name);
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

            ads = createAnnotationDescs(readMethod);
            for (int j = 0; j < ads.length; j++) {
                propertyDesc.setAnnotationDesc(ads[j]);
            }
            ads = createAnnotationDescs(writeMethod);
            for (int j = 0; j < ads.length; j++) {
                propertyDesc.setAnnotationDesc(ads[j]);
            }

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
            MethodDesc md = new MethodDescImpl(methods[i]);
            ads = createAnnotationDescs(methods[i]);
            for (int j = 0; j < ads.length; j++) {
                md.setAnnotationDesc(ads[j]);
            }
            Class<?> returnType = methods[i].getReturnType();
            md.setReturnTypeDesc(returnType.getName());
            if (clazz.getName().endsWith("Base") && returnType == String.class
                    && methods[i].getParameterTypes().length == 0) {
                // Baseクラスについては、引数なしで返り値がStringのメソッドについてはボディを用意する。
                // （他のもそうしたいが今はこれだけ）
                try {
                    String value = (String) methods[i].invoke(clazz
                            .newInstance(), new Object[0]);
                    BodyDesc bodyDesc = new BodyDescImpl("return "
                            + quote(value) + ";");
                    md.setBodyDesc(bodyDesc);
                } catch (Throwable ignore) {
                }
            }
            classDesc.setMethodDesc(md);
        }

        return classDesc;
    }

    String quote(String value) {
        if (value == null) {
            return "null";
        } else {
            return StringUtils.quoteString(value, '"');
        }
    }

    AnnotationDesc[] createAnnotationDescs(AnnotatedElement element) {
        if (element == null) {
            return new AnnotationDesc[0];
        }
        Annotation[] annotations = element.getAnnotations();
        AnnotationDesc[] ads = new AnnotationDesc[annotations.length];
        for (int i = 0; i < annotations.length; i++) {
            ads[i] = new AnnotationDescImpl(annotations[i]);
        }
        return ads;
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

        if (DescValidator.validate(typeDesc, classDescSet).isValid()) {
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
            try {
                writeSourceFile(classDescs[i], classDescBag.getClassDescSet());
            } catch (InvalidClassDescException ex) {
                // ソースファイルの生成に失敗した。
                classDescBag.remove(classDescs[i].getName());
                classDescBag.addAsFailed(classDescs[i], ex
                        .getLackingClassNames());
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

        Class baseClass = (clazz != null
                && clazz.getSuperclass().getName().equals(className + "Base") ? clazz
                .getSuperclass()
                : null);
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

    public void writeSourceFile(ClassDesc classDesc, ClassDescSet classDescSet)
            throws InvalidClassDescException {

        Result result = DescValidator.validate(classDesc, classDescSet);
        if (!result.isValid()) {
            throw new InvalidClassDescException(result.getClassNames());
        }

        writeString(sourceGenerator_.generateBaseSource(classDesc),
                getSourceFile(classDesc.getName() + "Base"));

        // gap側のクラスは存在しない場合のみ生成する。
        File sourceFile = getSourceFile(classDesc.getName());
        if (!sourceFile.exists()) {
            writeString(sourceGenerator_.generateGapSource(classDesc),
                    sourceFile);
        }
    }

    public void writeString(String string, File file) {

        if (string == null) {
            return;
        }
        file.getParentFile().mkdirs();
        try {
            writeString(string, new FileOutputStream(file));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void writeString(String string, OutputStream os) {

        try {
            if (string == null) {
                return;
            }
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

        if (componentName != null) {
            LocalHotdeployS2Container hotdeployContainer = getApplication()
                    .getHotdeployS2Container();
            String className = hotdeployContainer.getNamingConvention()
                    .fromComponentNameToClassName(componentName);
            if (className != null) {
                return className;
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

    public PropertyDescriptor getPropertyDescriptor(String className,
            String propertyName) {

        Class clazz = getClass(className);
        if (clazz == null || propertyName == null) {
            return null;
        }

        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < pds.length; i++) {
            if (pds[i].getName().equals(propertyName)) {
                return pds[i];
            }
        }
        return null;
    }

    public Template getTemplate(String path) {

        return templateProvider_.getTemplate(path);
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
                    log_.error("Can't read properties: " + file);
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

    @SuppressWarnings("unchecked")
    public void saveSourceCreatorProperties() {

        if (sourceCreatorProperties_ == null) {
            return;
        }

        MapProperties prop = new MapProperties(new TreeMap(
                sourceCreatorProperties_));

        File file = getSourceCreatorPropertiesFile();
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            prop.store(new BufferedOutputStream(fos), "ISO-8859-1");
            fos = null;
        } catch (IOException ex) {
            log_.error("Can't write properties: " + file);
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
                + namingConvention_.getSubApplicationRootPackageName();
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

        return applicationManager_.findContextApplication();
    }

    public ServletContext getServletContext() {

        return (ServletContext) getRootS2Container().getComponent(
                ServletContext.class);
    }

    public HttpServletRequest getHttpServletRequest() {

        return (HttpServletRequest) getRootS2Container().getComponent(
                HttpServletRequest.class);
    }

    public HttpServletResponse getHttpServletResponse() {

        return (HttpServletResponse) getRootS2Container().getComponent(
                HttpServletResponse.class);
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

    public TemplateProvider getTemplateProvider() {

        return templateProvider_;
    }

    public String filterResponse(String response) {

        String jsPrefix = "<script type=\"text/javascript\" src=\""
                + getHttpServletRequest().getContextPath() + PATH_PREFIX
                + "resource/js/";
        String jsSuffix = "\"></script>";
        return response.replace("</head>", jsPrefix + "prototype/prototype.js"
                + jsSuffix + jsPrefix + "scriptaculous/scriptaculous.js"
                + jsSuffix + jsPrefix + "sourceCreator.js" + jsSuffix
                + "</head>");
    }
}
