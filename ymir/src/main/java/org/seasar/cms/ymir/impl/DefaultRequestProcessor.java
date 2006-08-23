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
import org.seasar.cms.ymir.Application;
import org.seasar.cms.ymir.ApplicationManager;
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

    public static final String ACTION_RENDER = "_render";

    public static final String ATTR_PAGE = "PAGE";

    private ResponseConstructorSelector responseConstructorSelector_;

    private Updater[] updaters_ = new Updater[0];

    private Configuration configuration_;

    private ApplicationManager applicationManager_;

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

        MatchedPathMapping matched = findMatchedPathMapping(path, method);
        if (matched == null) {
            return PassthroughResponse.INSTANCE;
        }
        PathMapping mapping = matched.getPathMapping();
        if (mapping.isDenied() && Request.DISPATCHER_REQUEST.equals(dispatcher)) {
            throw new PageNotFoundException(path);
        }

        VariableResolver resolver = matched.getVariableResolver();
        String componentName = mapping.getComponentName(resolver);
        String actionName = mapping.getActionName(resolver);
        Request request = new RequestImpl(contextPath, path, method,
                dispatcher, parameterMap, fileParameterMap, mapping
                        .getPathInfo(resolver));

        if (Configuration.PROJECTSTATUS_DEVELOP.equals(getProjectStatus())
                && getApplication().isBeingDeveloped()) {
            for (int i = 0; i < updaters_.length; i++) {
                Response response = updaters_[i].update(path, request
                        .getMethod(), request);
                if (response != null) {
                    return response;
                }
            }
        }

        Response response = processRequest(request, componentName, actionName);

        // デフォルトパスが指定されており、かつpassthroughの場合でパスに
        // 対応するリソースが存在しない場合はデフォルトパスにリダイレクトする。
        if (response.getType() == Response.TYPE_PASSTHROUGH) {
            String defaultPath = mapping.getDefaultPath(resolver);
            if (defaultPath != null && !getApplication().isResourceExists(path)) {
                response = new RedirectResponse(defaultPath);
            }
        }

        return response;
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
            String actionName) {

        if (!getS2Container().hasComponentDef(componentName)) {
            return PassthroughResponse.INSTANCE;
        }

        ThreadContext context = getThreadContext();
        Object component;
        try {
            context.setComponent(Request.class, request);
            component = getS2Container().getComponent(componentName);
        } finally {
            context.setComponent(Request.class, null);
        }

        Response response = PassthroughResponse.INSTANCE;

        HttpServletRequest httpRequest = getHttpServletRequest();
        if (component != httpRequest.getAttribute(ATTR_PAGE)) {
            // 同一リクエストで直前に同一コンポーネントについて処理済みの
            // 場合はリクエストパラメータのinjectionもrenderメソッドの
            // 呼び出しもしない。そうでない場合のみ処理を行なう。

            try {
                beanUtilsBean_.populate(component, request.getParameterMap());
            } catch (Throwable t) {
                if (logger_.isDebugEnabled()) {
                    logger_.debug("Can't populate request parameters", t);
                }
            }
            try {
                beanUtilsBean_.copyProperties(component, request
                        .getFileParameterMap());
            } catch (Throwable t) {
                if (logger_.isDebugEnabled()) {
                    logger_.debug(
                            "Can't populate request parameters (FormFile)", t);
                }
            }

            if (Request.DISPATCHER_REQUEST.equals(request.getDispatcher())) {
                // Actionの呼び出しはdispatcherがREQUESTの時だけ。
                response = invokeAction(component, actionName);
            }

            if (response.getType() == Response.TYPE_PASSTHROUGH) {
                // dispatcherがREQUEST以外の場合やActionの呼び出し後に処理が
                // スルーされてきた場合は、画面描画のためのAction呼び出しを
                // 行なう。
                invokeAction(component, ACTION_RENDER);
            }

            // コンポーネント自体をrequestにバインドしておく。
            httpRequest.setAttribute(ATTR_PAGE, component);
        }

        return response;
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

    Response invokeAction(Object component, String actionName) {

        Method method = getActionMethod(component, actionName);
        if (method == null) {
            return PassthroughResponse.INSTANCE;
        }

        try {
            return constructResponse(component, method.getReturnType(), method
                    .invoke(component, new Object[0]));
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getCause());
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

        if (type == Void.TYPE) {
            return PassthroughResponse.INSTANCE;
        }

        ResponseConstructor constructor = responseConstructorSelector_
                .getResponseConstructor(type);
        if (constructor == null) {
            throw new ComponentNotFoundRuntimeException(
                    "Can't find ResponseConstructor for type '" + type
                            + "' in ResponseConstructorSelector");
        }

        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(
                    component.getClass().getClassLoader());
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
}
