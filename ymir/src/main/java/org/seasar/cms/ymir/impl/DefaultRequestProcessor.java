package org.seasar.cms.ymir.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.seasar.cms.pluggable.Configuration;
import org.seasar.cms.pluggable.ThreadContext;
import org.seasar.cms.ymir.AnnotationHandler;
import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.ApplicationManager;
import org.seasar.cms.ymir.AttributeHandler;
import org.seasar.cms.ymir.FormFile;
import org.seasar.cms.ymir.MatchedPathMapping;
import org.seasar.cms.ymir.PageNotFoundException;
import org.seasar.cms.ymir.PathMapping;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.RequestProcessor;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.Updater;
import org.seasar.cms.ymir.beanutils.FormFileArrayConverter;
import org.seasar.cms.ymir.beanutils.FormFileConverter;
import org.seasar.cms.ymir.response.constructor.ResponseConstructor;
import org.seasar.cms.ymir.response.constructor.ResponseConstructorSelector;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.log.Logger;
import org.seasar.kvasir.util.el.VariableResolver;

public class DefaultRequestProcessor implements RequestProcessor {

    public static final String ACTION_DEFAULT = "_default";

    public static final String ACTION_RENDER = "_render";

    public static final String ATTR_PAGE = "PAGE";

    public static final String PARAM_METHOD = "__ymir__method";

    private ResponseConstructorSelector responseConstructorSelector_;

    private Updater[] updaters_ = new Updater[0];

    private Configuration configuration_;

    private ApplicationManager applicationManager_;

    private AnnotationHandler annotationHandler_ = new DefaultAnnotationHandler();

    private final BeanUtilsBean beanUtilsBean_;

    private ThreadContext threadContext_;

    private final Logger logger_ = Logger.getLogger(getClass());

    public DefaultRequestProcessor() {

        ConvertUtilsBean convertUtilsBean = new ConvertUtilsBean();
        convertUtilsBean.register(new FormFileConverter(), FormFile.class);
        convertUtilsBean.register(new FormFileArrayConverter(),
                FormFile[].class);
        beanUtilsBean_ = new BeanUtilsBean(convertUtilsBean,
                new PropertyUtilsBean());
    }

    public Response process(String contextPath, String path, String method,
            String dispatcher, Map parameterMap, Map fileParameterMap)
            throws PageNotFoundException {

        boolean underDevelopment = Configuration.PROJECTSTATUS_DEVELOP
                .equals(getProjectStatus())
                && getApplication().isUnderDevelopment();
        if (underDevelopment) {
            method = correctMethod(method, parameterMap);
        }

        MatchedPathMapping matched = findMatchedPathMapping(path, method);
        if (matched == null) {
            return PassthroughResponse.INSTANCE;
        }
        if (matched.isDenied() && Request.DISPATCHER_REQUEST.equals(dispatcher)) {
            throw new PageNotFoundException(path);
        }

        String componentName = matched.getComponentName();
        String actionName = matched.getActionName();
        Request request = new RequestImpl(contextPath, path, method,
                dispatcher, parameterMap, fileParameterMap, matched
                        .getPathInfo());

        Response response = processRequest(request, componentName, actionName,
                matched.getDefaultReturnValue());

        if (underDevelopment) {
            for (int i = 0; i < updaters_.length; i++) {
                Response newResponse = updaters_[i].update(path, method,
                        request, response);
                if (newResponse != response) {
                    return newResponse;
                }
            }
        }

        return response;
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

    public Object backupForInclusion() {
        return getHttpServletRequest().getAttribute(ATTR_PAGE);
    }

    public void restoreForInclusion(Object backupped) {
        getHttpServletRequest().setAttribute(ATTR_PAGE, backupped);
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

    Response processRequest(Request request, String componentName,
            String actionName, Object defaultReturnValue) {

        // dispatchがREQUESTの時は、
        // ・pathに対応するcomponentがあればactionを実行する。actionの実行結果が
        //   passthroughの時は、デフォルトの返り値が指定されていれば、 デフォルトの返り値
        //   に対応するresponseに置き換える。
        // ・pathに対応するcomponentがなければresponseの値はpassthroughとする。
        //   デフォルトの返り値が指定されている場合はデフォルトの返り値に対応するresponseとする。
        // ・responseがリクエストパスへのforwardである場合はpassthroughをresponseとする。
        // ・最終的なresponseがpassthroughでかつcomponentがあれば、レンダリングのための
        //   メソッドを呼び出す。
        // ・最終的なresponseがforwardでかつforward先に対応するコンポーネントが存在しない場合は
        //   レンダリングのためのメソッドを呼び出す。
        // dispatchがREQUESTでない時は、
        // ・pathに対応するcomponentがあればレンダリングのためのメソッドを呼び出す。

        Response response = PassthroughResponse.INSTANCE;

        Object component = null;
        S2Container s2container = getS2Container();
        if (s2container.hasComponentDef(componentName)) {
            ThreadContext context = getThreadContext();
            try {
                context.setComponent(Request.class, request);
                component = s2container.getComponent(componentName);
            } finally {
                context.setComponent(Request.class, null);
            }
        }

        if (Request.DISPATCHER_REQUEST.equals(request.getDispatcher())) {
            if (component != null) {
                prepareForComponent(component, request);

                response = normalizeResponse(invokeAction(component,
                        actionName, ACTION_DEFAULT, defaultReturnValue),
                        request.getPath());

                if (shouldRender(response)) {
                    // 画面描画のためのAction呼び出しを行なう。
                    invokeAction(component, ACTION_RENDER, null, null);
                }

                finishForComponent(component);
            } else {
                response = normalizeResponse(constructResponseFromReturnValue(
                        null, defaultReturnValue), request.getPath());
            }
        } else {
            if (component != null) {
                prepareForComponent(component, request);

                // 画面描画のためのAction呼び出しを行なう。
                invokeAction(component, ACTION_RENDER, null, null);

                finishForComponent(component);
            }
        }

        return response;
    }

    boolean shouldRender(Response response) {

        if (response.getType() == Response.TYPE_PASSTHROUGH) {
            return true;
        }

        if (response.getType() == Response.TYPE_FORWARD) {
            MatchedPathMapping matched = findMatchedPathMapping(response
                    .getPath(), Request.METHOD_GET);
            if (matched == null
                    || matched.isDenied()
                    || !getS2Container().hasComponentDef(
                            matched.getComponentName())) {
                return true;
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

    void finishForComponent(Object component) {
        // 各コンテキストに属性をoutjectする。
        outjectContextAttributes(component);

        // コンポーネント自体をrequestにバインドしておく。
        getHttpServletRequest().setAttribute(ATTR_PAGE, component);
    }

    Response normalizeResponse(Response response, String path) {
        if (response.getType() == Response.TYPE_FORWARD
                && path.equals(response.getPath())) {
            return PassthroughResponse.INSTANCE;
        } else {
            return response;
        }
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

    HttpServletRequest getHttpServletRequest() {

        return (HttpServletRequest) getRootS2Container().getComponent(
                HttpServletRequest.class);
    }

    Response invokeAction(Object component, String actionName,
            String defaultActionName, Object defaultReturnValue) {

        Response response = PassthroughResponse.INSTANCE;

        Method method = getActionMethod(component, actionName);
        if (method == null && defaultActionName != null) {
            method = getActionMethod(component, defaultActionName);
        }
        if (method != null) {
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
        }

        if (response == PassthroughResponse.INSTANCE) {
            response = constructResponseFromReturnValue(component,
                    defaultReturnValue);
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
}
