package org.seasar.cms.ymir.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.seasar.cms.pluggable.Configuration;
import org.seasar.cms.pluggable.ThreadContext;
import org.seasar.cms.ymir.AnnotationHandler;
import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.ApplicationManager;
import org.seasar.cms.ymir.AttributeContainer;
import org.seasar.cms.ymir.AttributeHandler;
import org.seasar.cms.ymir.Constraint;
import org.seasar.cms.ymir.ConstraintViolationException;
import org.seasar.cms.ymir.FormFile;
import org.seasar.cms.ymir.MatchedPathMapping;
import org.seasar.cms.ymir.PageNotFoundException;
import org.seasar.cms.ymir.PathMapping;
import org.seasar.cms.ymir.PermissionDeniedException;
import org.seasar.cms.ymir.RedirectionPathResolver;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.RequestProcessor;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.Updater;
import org.seasar.cms.ymir.beanutils.FormFileArrayConverter;
import org.seasar.cms.ymir.beanutils.FormFileConverter;
import org.seasar.cms.ymir.response.PassthroughResponse;
import org.seasar.cms.ymir.response.constructor.ResponseConstructor;
import org.seasar.cms.ymir.response.constructor.ResponseConstructorSelector;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.log.Logger;
import org.seasar.framework.util.Disposable;
import org.seasar.framework.util.DisposableUtil;
import org.seasar.kvasir.util.el.VariableResolver;

import net.skirnir.freyja.render.Note;
import net.skirnir.freyja.render.Notes;

public class DefaultRequestProcessor implements RequestProcessor {

    public static final String ACTION_DEFAULT = "_default";

    public static final String ACTION_RENDER = "_render";

    public static final String ATTR_SELF = "self";

    public static final String PARAM_METHOD = "__ymir__method";

    private static final String ATTR_NOTES = "notes";

    private ResponseConstructorSelector responseConstructorSelector_;

    private Updater[] updaters_ = new Updater[0];

    private Configuration configuration_;

    private ApplicationManager applicationManager_;

    private AnnotationHandler annotationHandler_ = new DefaultAnnotationHandler();

    private RedirectionPathResolver redirectionPathResolver_ = new DefaultRedirectionPathResolver();

    private final PropertyUtilsBean propertyUtilsBean_ = new PropertyUtilsBean();

    private final BeanUtilsBean beanUtilsBean_;

    private ThreadContext threadContext_;

    private final Logger logger_ = Logger.getLogger(getClass());

    public DefaultRequestProcessor() {

        ConvertUtilsBean convertUtilsBean = new ConvertUtilsBean();
        convertUtilsBean.register(new FormFileConverter(), FormFile.class);
        convertUtilsBean.register(new FormFileArrayConverter(),
                FormFile[].class);
        beanUtilsBean_ = new BeanUtilsBean(convertUtilsBean, propertyUtilsBean_);
        DisposableUtil.add(new Disposable() {
            public void dispose() {
                propertyUtilsBean_.clearDescriptors();
            }
        });
    }

    public Request prepareForProcessing(String contextPath, String path,
            String method, String dispatcher, Map parameterMap,
            Map fileParameterMap, AttributeContainer attributeContainer)
            throws PageNotFoundException {

        if (isUnderDevelopment()) {
            method = correctMethod(method, parameterMap);
        }

        MatchedPathMapping matched = findMatchedPathMapping(path, method);
        if (matched == null) {
            return null;
        }
        if (matched.isDenied()) {
            throw new PageNotFoundException(path);
        }

        return new RequestImpl(contextPath, path, method, dispatcher,
                parameterMap, fileParameterMap, attributeContainer, matched);
    }

    String correctMethod(String method, Map parameterMap) {
        String[] values = (String[]) parameterMap.get(PARAM_METHOD);
        if (values != null && values.length > 0) {
            return values[0];
        } else {
            return method;
        }
    }

    String strip(String path) {
        int question = path.indexOf('?');
        if (question >= 0) {
            path = path.substring(0, question);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public Object backupForInclusion(AttributeContainer attributeContainer) {
        return attributeContainer.getAttribute(ATTR_SELF);
    }

    public void restoreForInclusion(AttributeContainer attributeContainer,
            Object backupped) {
        attributeContainer.setAttribute(ATTR_SELF, backupped);
    }

    Application getApplication() {
        return applicationManager_.findContextApplication();
    }

    PathMapping[] getPathMappings() {
        return getApplication().getPathMappingProvider().getPathMappings();
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

    String getProjectStatus() {

        if (configuration_ != null) {
            return configuration_.getProperty(Configuration.KEY_PROJECTSTATUS);
        } else {
            return null;
        }
    }

    public MatchedPathMapping findMatchedPathMapping(String path, String method) {

        VariableResolver resolver = null;
        PathMapping[] pathMappings = getPathMappings();
        if (pathMappings != null) {
            for (int i = 0; i < pathMappings.length; i++) {
                resolver = pathMappings[i].match(path, method);
                if (resolver != null) {
                    return new MatchedPathMapping(pathMappings[i], resolver);
                }
            }
        }
        return null;
    }

    public Response process(Request request)
            throws ConstraintViolationException {

        if (request == null) {
            return PassthroughResponse.INSTANCE;
        }

        Object component = null;
        S2Container s2container = getS2Container();
        if (s2container.hasComponentDef(request.getComponentName())) {
            ThreadContext context = getThreadContext();
            try {
                context.setComponent(Request.class, request);
                component = s2container
                        .getComponent(request.getComponentName());
            } finally {
                context.setComponent(Request.class, null);
            }
        }

        Response response = PassthroughResponse.INSTANCE;
        if (component != null) {
            prepareForComponent(component, request);

            // リクエストに対応するアクションの呼び出しを行なう。
            response = normalizeResponse(constructResponse(invokeAction(
                    component, getActionMethod(component, request
                            .getActionName(), ACTION_DEFAULT, request
                            .isDispatchingByParameter(), request), request,
                    true), component, request.getDefaultReturnValue()), request
                    .getPath());

            // 画面描画のためのAction呼び出しを行なう。
            // （画面描画のためのAction呼び出しの際にはAction付随の制約以外の
            // 制約チェックを行なわない。）
            invokeAction(component, getActionMethod(component, ACTION_RENDER,
                    null, false, request), request, false);

            finishForComponent(component, request);
        } else {
            // componentがnullになるのは、component名が割り当てられているのにまだ
            // 対応するcomponentクラスが作成されていない場合。
            // その場合自動生成機能でクラスやテンプレートの自動生成が適切にできるように、
            // デフォルト値からResponseを作るようにしている。
            // （例えば、リクエストパス名がテンプレートパス名ではない場合に、リクエストパス名で
            // テンプレートが作られてしまうとうれしくない。）
            response = normalizeResponse(constructResponseFromReturnValue(
                    component, request.getDefaultReturnValue()), request
                    .getPath());
        }

        if (logger_.isDebugEnabled()) {
            logger_.debug("FINAL RESPONSE: " + response);
        }

        if (isUnderDevelopment()) {
            if (response.getType() == Response.TYPE_PASSTHROUGH
                    || response.getType() == Response.TYPE_FORWARD) {
                for (int i = 0; i < updaters_.length; i++) {
                    Response newResponse = updaters_[i].update(request,
                            response);
                    if (newResponse != response) {
                        return newResponse;
                    }
                }
            }
        }

        return response;
    }

    Response normalizeResponse(Response response, String path) {
        if (response.getType() == Response.TYPE_FORWARD
                && response.getPath().equals(path)) {
            return PassthroughResponse.INSTANCE;
        } else {
            return response;
        }
    }

    boolean isUnderDevelopment() {
        return Configuration.PROJECTSTATUS_DEVELOP.equals(getProjectStatus())
                && getApplication().isUnderDevelopment();
    }

    Notes confirmConstraint(Object component, Method action, Request request,
            boolean alsoConfirmCommonConstraints)
            throws ConstraintViolationException {

        Notes notes = new Notes();
        Constraint[] constraint = annotationHandler_.getConstraints(component,
                action, alsoConfirmCommonConstraints);
        for (int i = 0; i < constraint.length; i++) {
            try {
                constraint[i].confirm(component, request);
            } catch (PermissionDeniedException ex) {
                throw ex;
            } catch (ConstraintViolationException ex) {
                if (ex.hasMessage()) {
                    ConstraintViolationException.Message[] messages = ex
                            .getMessages();
                    for (int j = 0; j < messages.length; j++) {
                        notes.add(new Note(messages[j].getKey(), messages[j]
                                .getParameters()));
                    }
                } else {
                    throw ex;
                }
            }
        }
        if (notes.size() > 0) {
            return notes;
        } else {
            return null;
        }
    }

    Object getRequestComponent(Request request) {

        return request.getAttribute(ATTR_SELF);
    }

    void prepareForComponent(Object component, Request request) {
        // 各コンテキストが持つ属性をinjectする。
        injectContextAttributes(component);

        // リクエストパラメータをinjectする。
        try {
            beanUtilsBean_.populate(component, request.getParameterMap());
        } catch (Throwable t) {
            if (logger_.isDebugEnabled()) {
                logger_.debug("Can't populate request parameters", t);
            }
        }

        // FormFileのリクエストパラメータをinjectする。
        try {
            beanUtilsBean_.copyProperties(component, request
                    .getFileParameterMap());
        } catch (Throwable t) {
            if (logger_.isDebugEnabled()) {
                logger_
                        .debug("Can't populate request parameters (FormFile)",
                                t);
            }
        }
    }

    void finishForComponent(Object component, Request request) {
        // 各コンテキストに属性をoutjectする。
        outjectContextAttributes(component);

        // コンポーネント自体をattributeとしてバインドしておく。
        request.setAttribute(ATTR_SELF, component);
    }

    void injectContextAttributes(Object component) {
        AttributeHandler[] handlers = annotationHandler_
                .getInjectedScopeAttributes(component);
        for (int i = 0; i < handlers.length; i++) {
            handlers[i].injectTo(component);
        }
    }

    void outjectContextAttributes(Object component) {
        AttributeHandler[] handlers = annotationHandler_
                .getOutjectedScopeAttributes(component);
        for (int i = 0; i < handlers.length; i++) {
            handlers[i].outjectFrom(component);
        }
    }

    ThreadContext getThreadContext() {
        if (threadContext_ == null) {
            threadContext_ = (ThreadContext) getRootS2Container().getComponent(
                    ThreadContext.class);
        }
        return threadContext_;
    }

    Response invokeAction(Object component, Method action, Request request,
            boolean alsoConfirmCommonConstraints)
            throws ConstraintViolationException {

        Response response = PassthroughResponse.INSTANCE;

        if (action != null) {
            if (logger_.isDebugEnabled()) {
                logger_.debug("INVOKE: " + component.getClass().getName() + "#"
                        + action);
            }

            Notes notes = confirmConstraint(component, action, request,
                    alsoConfirmCommonConstraints);
            if (notes == null) {
                Object returnValue;
                try {
                    returnValue = action.invoke(component, new Object[0]);
                } catch (IllegalArgumentException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                } catch (InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
                response = constructResponse(component, action.getReturnType(),
                        returnValue);
            } else {
                request.setAttribute(ATTR_NOTES, notes);
            }

            if (logger_.isDebugEnabled()) {
                logger_.debug("RESPONSE: " + response);
            }
        }

        return response;
    }

    Method getActionMethod(Object component, String actionName,
            String defaultActionName, boolean dispatchingByParamter,
            Request request) {
        if (dispatchingByParamter) {
            Method[] methods = component.getClass().getMethods();
            if (logger_.isDebugEnabled()) {
                logger_
                        .debug("getActionMethod: dispatchingByRequestParameter=true. search "
                                + component.getClass()
                                + " for "
                                + actionName
                                + " method...");
            }
            List list = new ArrayList();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getParameterTypes().length > 0) {
                    continue;
                }
                String name = methods[i].getName();
                if (name.startsWith(actionName)) {
                    String parameterName = request.extractParameterName(name
                            .substring(actionName.length()));
                    if (parameterName != null
                            && request.getParameter(parameterName) != null) {
                        if (logger_.isDebugEnabled()) {
                            logger_.debug("getActionMethod: Found: "
                                    + methods[i]);
                        }
                        return methods[i];
                    }
                }
                list.add(methods[i]);
            }
            if (defaultActionName != null) {
                if (logger_.isDebugEnabled()) {
                    logger_.debug("getActionMethod: search "
                            + component.getClass() + " for "
                            + defaultActionName + " method...");
                }
                for (Iterator itr = list.iterator(); itr.hasNext();) {
                    Method method = (Method) itr.next();
                    String name = method.getName();
                    if (name.startsWith(defaultActionName)) {
                        String parameterName = request
                                .extractParameterName(name
                                        .substring(defaultActionName.length()));
                        if (parameterName != null
                                && request.getParameter(parameterName) != null) {
                            if (logger_.isDebugEnabled()) {
                                logger_.debug("getActionMethod: Found: "
                                        + method);
                            }
                            return method;
                        }
                    }
                }
            }
        }

        Method method = getActionMethod(component, actionName);
        if (method == null && defaultActionName != null) {
            method = getActionMethod(component, defaultActionName);
        }

        if (logger_.isDebugEnabled()) {
            logger_.debug("getActionMethod: Found: " + method);
        }
        return method;
    }

    Response constructResponse(Response response, Object component,
            Object returnValue) {
        if (response.getType() == Response.TYPE_PASSTHROUGH) {
            response = constructResponseFromReturnValue(component, returnValue);
        }

        if (logger_.isDebugEnabled()) {
            logger_.debug("[4]RESPONSE: " + response);
        }

        return response;
    }

    Response constructResponseFromReturnValue(Object component,
            Object returnValue) {
        if (returnValue != null) {
            return constructResponse(component, returnValue.getClass(),
                    returnValue);
        } else {
            return PassthroughResponse.INSTANCE;
        }
    }

    public Method getActionMethod(Object component, String actionName) {

        try {
            return component.getClass().getMethod(actionName, new Class[0]);
        } catch (SecurityException ex) {
            return null;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    Response constructResponse(Object component, Class type, Object returnValue) {

        ResponseConstructor constructor = responseConstructorSelector_
                .getResponseConstructor(type);
        if (constructor == null) {
            throw new ComponentNotFoundRuntimeException(
                    "Can't find ResponseConstructor for type '" + type
                            + "' in ResponseConstructorSelector");
        }

        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        try {
            if (component != null) {
                Thread.currentThread().setContextClassLoader(
                        component.getClass().getClassLoader());
            }
            return constructor.constructResponse(component, returnValue);
        } finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }

    public void setResponseConstructorSelector(
            ResponseConstructorSelector responseConstructorSelector) {

        responseConstructorSelector_ = responseConstructorSelector;
    }

    public void setUpdaters(Updater[] updaters) {

        updaters_ = updaters;
    }

    public void setConfiguration(Configuration configuration) {

        configuration_ = configuration;
    }

    public void setApplicationManager(ApplicationManager applicationManager) {

        applicationManager_ = applicationManager;
    }

    public void setAnnotationHandler(AnnotationHandler annotationHandler) {

        annotationHandler_ = annotationHandler;
    }

    public void setRedirectionPathResolver(
            RedirectionPathResolver redirectionPathResolver) {

        redirectionPathResolver_ = redirectionPathResolver;
    }
}
