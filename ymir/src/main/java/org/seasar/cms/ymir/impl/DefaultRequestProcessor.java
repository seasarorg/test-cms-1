package org.seasar.cms.ymir.impl;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.seasar.cms.pluggable.ThreadContext;
import org.seasar.cms.ymir.FormFile;
import org.seasar.cms.ymir.Globals;
import org.seasar.cms.ymir.MatchedPathMapping;
import org.seasar.cms.ymir.PageNotFoundException;
import org.seasar.cms.ymir.PathMapping;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.RequestProcessor;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.Updater;
import org.seasar.cms.ymir.YmirConfiguration;
import org.seasar.cms.ymir.beanutils.FormFileArrayConverter;
import org.seasar.cms.ymir.beanutils.FormFileConverter;
import org.seasar.cms.ymir.response.constructor.ResponseConstructor;
import org.seasar.cms.ymir.response.constructor.ResponseConstructorSelector;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.log.Logger;
import org.seasar.kvasir.util.el.VariableResolver;

public class DefaultRequestProcessor implements RequestProcessor {

    public static final String ACTION_RENDER = "_render";

    public static final String ATTR_PAGE = "PAGE";

    private PathMapping[] pathMappings_;

    private ResponseConstructorSelector responseConstructorSelector_;

    private S2Container container_;

    private Updater[] updaters_ = new Updater[0];

    private YmirConfiguration configuration_;

    private final BeanUtilsBean beanUtilsBean_;

    private final Logger logger_ = Logger.getLogger(getClass());

    private ThreadContext threadContext_;

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

        if (YmirConfiguration.PROJECTSTATUS_DEVELOP.equals(getProjectStatus())) {
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
            if (defaultPath != null && !isResourceExists(path)) {
                response = new RedirectResponse(defaultPath);
            }
        }

        return response;
    }

    boolean isResourceExists(String path) {

        return new File(configuration_
            .getProperty(Globals.KEY_WEBAPPROOT), path).exists();
    }

    String getProjectStatus() {

        if (configuration_ != null) {
            return configuration_.getProperty(YmirConfiguration.KEY_PROJECTSTATUS);
        } else {
            return null;
        }
    }

    public MatchedPathMapping findMatchedPathMapping(String path, String method) {

        VariableResolver resolver = null;
        if (pathMappings_ != null) {
            for (int i = 0; i < pathMappings_.length; i++) {
                resolver = pathMappings_[i].match(path, method);
                if (resolver != null) {
                    return new MatchedPathMapping(pathMappings_[i], resolver);
                }
            }
        }
        return null;
    }

    Response processRequest(Request request, String componentName,
        String actionName) {

        if (!container_.hasComponentDef(componentName)) {
            return PassthroughResponse.INSTANCE;
        }

        ThreadContext context = getThreadContext();
        Object component;
        try {
            context.setComponent(Request.class, request);
            component = container_.getComponent(componentName);
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
            threadContext_ = (ThreadContext) container_.getRoot().getComponent(
                ThreadContext.class);
        }
        return threadContext_;
    }

    HttpServletRequest getHttpServletRequest() {

        return ((HttpServletRequest) container_.getRoot().getExternalContext()
            .getRequest());
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

    public void setS2Container(S2Container container) {

        container_ = container;
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

    public PathMapping[] getPathMappings() {

        return pathMappings_;
    }

    public void setPathMappings(PathMapping[] pathMappings) {

        pathMappings_ = pathMappings;
    }

    public void addPathMapping(String patternString,
        String componentNameTemplate, String actionNameTemplate,
        String pathInfoTemplate, String defaultPathTemplate) {

        pathMappings_ = addPathMapping(pathMappings_, new PathMappingImpl(
            patternString, componentNameTemplate, actionNameTemplate,
            pathInfoTemplate, defaultPathTemplate));
    }

    PathMapping[] addPathMapping(PathMapping[] patterns, PathMapping pattern) {

        PathMapping[] newPatterns;
        if (patterns == null) {
            newPatterns = new PathMapping[] { pattern };
        } else {
            newPatterns = new PathMapping[patterns.length + 1];
            System.arraycopy(patterns, 0, newPatterns, 0, patterns.length);
            newPatterns[patterns.length] = pattern;
        }
        return newPatterns;
    }

    public void setResponseConstructorSelector(
        ResponseConstructorSelector responseConstructorSelector) {

        responseConstructorSelector_ = responseConstructorSelector;
    }

    public void setUpdaters(Updater[] updaters) {

        updaters_ = updaters;
    }

    public void setConfiguration(YmirConfiguration configuration) {

        configuration_ = configuration;
    }
}
