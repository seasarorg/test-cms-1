package org.seasar.cms.ymir.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.RequestProcessor;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.ResponsePathNormalizer;
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

    private ResponsePathNormalizer responsePathNormalizer_ = new DefaultResponsePathNormalizer();

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

        boolean underDevelopment = Configuration.PROJECTSTATUS_DEVELOP
                .equals(getProjectStatus())
                && getApplication().isUnderDevelopment();
        if (underDevelopment) {
            method = correctMethod(method, parameterMap);
        }

        MatchedPathMapping matched = findMatchedPathMapping(path, method);
        if (matched == null) {
            return null;
        }
        if (matched.isDenied() && Request.DISPATCHER_REQUEST.equals(dispatcher)) {
            throw new PageNotFoundException(path);
        }

        return new RequestImpl(contextPath, path, method, dispatcher,
                parameterMap, fileParameterMap, attributeContainer, matched
                        .getComponentName(), matched.getActionName(), matched
                        .getPathInfo(), matched.getDefaultReturnValue());
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
        return applicationManager_.getContextApplication();
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

        // dispatchがREQUESTの時は、
        // ・pathに対応するcomponentがあればactionを実行する。actionの実行結果が
        //   passthroughの時は、デフォルトの返り値が指定されていれば、 デフォルトの返り値
        //   に対応するresponseに置き換える。
        // ・pathに対応するcomponentがなければresponseの値はpassthroughとする。
        //   デフォルトの返り値が指定されている場合はデフォルトの返り値に対応するresponseとする。
        // ・responseがリクエストパスへのforwardである場合はpassthroughをresponseとする。
        // ・最終的なresponseがpassthroughでかつcomponentがあれば、レンダリングのための
        //   メソッドを呼び出す。
        // ・最終的なresponseがforwardである場合、forward先に対応するコンポーネント名がないか
        //   対応するコンポーネント名が元々のcomponentの名前と一致するならば
        //   レンダリングのためのメソッドを呼び出す。
        // dispatchがREQUESTでない時は、
        // ・pathに対応するcomponentがあって、それがリクエスト時のcomponentと一致しないならば
        //   レンダリングのためのメソッドを呼び出す。

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
        boolean rendered = false;
        if (component != null) {
            if (Request.DISPATCHER_REQUEST.equals(request.getDispatcher())) {
                prepareForComponent(component, request);

                response = normalizeResponse(constructResponse(invokeAction(
                        component, request.getActionName(), ACTION_DEFAULT,
                        request, true), component, request
                        .getDefaultReturnValue()), request, component);

                if (shouldRender(response, request.getComponentName())) {
                    // 画面描画のためのAction呼び出しを行なう。
                    // （画面描画のためのAction呼び出しの際にはAction付随の制約以外の
                    // 制約チェックを行なわない。）
                    invokeAction(component, ACTION_RENDER, null, request, false);
                    rendered = true;
                }

                finishForComponent(component, request);
            } else {
                if (component != getRequestComponent(request)) {
                    // 画面描画のためのAction呼び出しをまだ行なっていないので行なう。

                    prepareForComponent(component, request);

                    // （画面描画のためのAction呼び出しの際にはAction付随の制約以外の
                    // 制約チェックを行なわない。）
                    invokeAction(component, ACTION_RENDER, null, request, false);
                    rendered = true;

                    finishForComponent(component, request);
                }
                response = normalizeResponse(constructResponseFromReturnValue(
                        component, request.getDefaultReturnValue()), request,
                        component);
            }
        }

        if (logger_.isDebugEnabled()) {
            logger_.debug("FINAL RESPONSE: " + response);
        }

        boolean underDevelopment = Configuration.PROJECTSTATUS_DEVELOP
                .equals(getProjectStatus())
                && getApplication().isUnderDevelopment();
        if (underDevelopment && rendered) {
            for (int i = 0; i < updaters_.length; i++) {
                Response newResponse = updaters_[i].update(request, response);
                if (newResponse != response) {
                    return newResponse;
                }
            }
        }

        return response;
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

    boolean shouldRender(Response response, String componentName) {

        if (response.getType() == Response.TYPE_PASSTHROUGH) {
            return true;
        }

        if (response.getType() == Response.TYPE_FORWARD) {
            MatchedPathMapping matched = findMatchedPathMapping(response
                    .getPath(), Request.METHOD_GET);
            if (matched == null) {
                return true;
            } else {
                if (logger_.isDebugEnabled()) {
                    logger_
                            .debug("NOT RENDER because component is different: target component="
                                    + componentName
                                    + ", next component="
                                    + matched.getComponentName());
                }
            }
        } else {
            if (logger_.isDebugEnabled()) {
                logger_
                        .debug("NOT RENDER because response directs to transit to different page: response="
                                + response);
            }
        }
        return false;
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

    Response normalizeResponse(Response response, Request request,
            Object component) {

        return normalizeResponse(response, request, component, request
                .getPath(), request.getComponentName());
    }

    Response normalizeResponse(Response response, Request request,
            Object component, String path, String componentName) {

        int type = response.getType();
        if (type == Response.TYPE_FORWARD || type == Response.TYPE_REDIRECT) {
            String normalized = responsePathNormalizer_.normalize(response
                    .getPath(), request);
            if (type == Response.TYPE_FORWARD) {
                if (path.equals(normalized)) {
                    return PassthroughResponse.INSTANCE;
                } else {
                    // フォワード先のコンポーネントが現在のコンポーネントと同じ場合、
                    // フォワード前にrenderingを行なうことになる。
                    // ということは、フォワード後には
                    // （フォワード時にはアクションは呼ばれないしrenderingも行なわれないので）
                    // デフォルトの返り値に従って再度フォワードされるだけになる。
                    // デフォルトの返り値が同じコンポーネントへのforwardである限り、
                    // 同様にフォワード後には何の処理も行なわれないので、
                    // 結局「フォワードでかつフォワード先のコンポーネントが同じ限り
                    // フォワード処理をスキップしてよい」ということになる。
                    MatchedPathMapping matched = findMatchedPathMapping(
                            normalized, Request.METHOD_GET);
                    if (matched != null
                            && componentName.equals(matched.getComponentName())) {
                        Response constructed = constructResponseFromReturnValue(
                                component, matched.getDefaultReturnValue());
                        Response finalResponse = normalizeResponse(constructed,
                                request, component, normalized, componentName);
                        if (finalResponse.getType() == Response.TYPE_PASSTHROUGH) {
                            constructed.setPath(normalized);
                            return constructed;
                        } else {
                            return finalResponse;
                        }
                    }
                }
            }
            response.setPath(normalized);
        }

        return response;
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

    Response invokeAction(Object component, String actionName,
            String defaultActionName, Request request,
            boolean alsoConfirmCommonConstraints)
            throws ConstraintViolationException {

        if (logger_.isDebugEnabled()) {
            logger_.debug("[1]INVOKE: " + component.getClass().getName() + "#"
                    + actionName);
        }
        Response response = PassthroughResponse.INSTANCE;

        Method method = getActionMethod(component, actionName);
        if (method == null && defaultActionName != null) {
            method = getActionMethod(component, defaultActionName);
            if (logger_.isDebugEnabled()) {
                logger_.debug("[2]INVOKE: " + component.getClass().getName()
                        + "#" + defaultActionName);
            }
        }
        if (method != null) {
            Notes notes = confirmConstraint(component, method, request,
                    alsoConfirmCommonConstraints);
            if (notes == null) {
                Object returnValue;
                try {
                    returnValue = method.invoke(component, new Object[0]);
                } catch (IllegalArgumentException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                } catch (InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
                response = constructResponse(component, method.getReturnType(),
                        returnValue);
            } else {
                request.setAttribute(ATTR_NOTES, notes);
            }
        }

        if (logger_.isDebugEnabled()) {
            logger_.debug("[3]RESPONSE: " + response);
        }

        return response;
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

    //
    //
    //
    //        if (response.getType() == Response.TYPE_FORWARD
    //                && path.equals(response.getPath())) {
    //            response = PassthroughResponse.INSTANCE;
    //        }
    //        return response;
    //    }

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

    public void setResponsePathNormalizer(
            ResponsePathNormalizer responsePathNormalizer) {
        responsePathNormalizer_ = responsePathNormalizer;
    }
}
