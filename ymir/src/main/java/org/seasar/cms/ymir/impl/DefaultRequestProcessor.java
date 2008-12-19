package org.seasar.cms.ymir.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.cms.pluggable.ThreadContext;
import org.seasar.cms.ymir.AnnotationHandler;
import org.seasar.cms.ymir.AttributeContainer;
import org.seasar.cms.ymir.Constraint;
import org.seasar.cms.ymir.ConstraintViolatedException;
import org.seasar.cms.ymir.FormFile;
import org.seasar.cms.ymir.MatchedPathMapping;
import org.seasar.cms.ymir.Note;
import org.seasar.cms.ymir.Notes;
import org.seasar.cms.ymir.PageNotFoundException;
import org.seasar.cms.ymir.PathMapping;
import org.seasar.cms.ymir.PermissionDeniedException;
import org.seasar.cms.ymir.RedirectionPathResolver;
import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.RequestProcessor;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.ScopeAttribute;
import org.seasar.cms.ymir.Updater;
import org.seasar.cms.ymir.ValidationFailedException;
import org.seasar.cms.ymir.WrappingRuntimeException;
import org.seasar.cms.ymir.Ymir;
import org.seasar.cms.ymir.beanutils.FormFileArrayConverter;
import org.seasar.cms.ymir.beanutils.FormFileConverter;
import org.seasar.cms.ymir.response.PassthroughResponse;
import org.seasar.cms.ymir.response.constructor.ResponseConstructor;
import org.seasar.cms.ymir.response.constructor.ResponseConstructorSelector;
import org.seasar.cms.ymir.util.BeanUtils;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.util.Disposable;
import org.seasar.framework.util.DisposableUtil;
import org.seasar.kvasir.util.el.VariableResolver;

public class DefaultRequestProcessor implements RequestProcessor {

    public static final String PARAM_METHOD = "__ymir__method";

    private Ymir ymir_;

    private ResponseConstructorSelector responseConstructorSelector_;

    private Updater[] updaters_ = new Updater[0];

    private AnnotationHandler annotationHandler_ = new DefaultAnnotationHandler();

    private RedirectionPathResolver redirectionPathResolver_ = new DefaultRedirectionPathResolver();

    private final PropertyUtilsBean propertyUtilsBean_ = new PropertyUtilsBean();

    private final BeanUtilsBean beanUtilsBean_;

    private ThreadContext threadContext_;

    private final Log log_ = LogFactory.getLog(getClass());

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
            Map fileParameterMap, AttributeContainer attributeContainer,
            Locale locale) {

        if (ymir_.isUnderDevelopment()) {
            method = correctMethod(method, parameterMap);
        }

        MatchedPathMapping matched = findMatchedPathMapping(path, method);

        return new RequestImpl(contextPath, path, method, dispatcher,
                parameterMap, fileParameterMap, attributeContainer, locale,
                matched);
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

    PathMapping[] getPathMappings() {
        return ymir_.getApplication().getPathMappingProvider()
                .getPathMappings();
    }

    ServletContext getServletContext() {
        return (ServletContext) getRootS2Container().getComponent(
                ServletContext.class);
    }

    S2Container getS2Container() {
        return ymir_.getApplication().getS2Container();
    }

    S2Container getRootS2Container() {
        return SingletonS2ContainerFactory.getContainer();
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

    public Response process(Request request) throws PageNotFoundException,
            PermissionDeniedException {

        if (request.isDenied()) {
            throw new PageNotFoundException(request.getPath());
        }

        Response response = PassthroughResponse.INSTANCE;
        if (request.isMatched()) {
            Object component = null;
            S2Container s2container = getS2Container();
            if (s2container.hasComponentDef(request.getComponentName())) {
                ThreadContext context = getThreadContext();
                try {
                    context.setComponent(Request.class, request);
                    component = s2container.getComponent(request
                            .getComponentName());
                } finally {
                    context.setComponent(Request.class, null);
                }
            }

            if (component != null) {
                prepareForComponent(component, request);

                // リクエストに対応するアクションの呼び出しを行なう。
                response = normalizeResponse(adjustResponse(request,
                        invokeAction(component, getActionMethod(component,
                                request.getActionName(), ACTION_DEFAULT,
                                request.isDispatchingByParameter(), request),
                                request, true), component), request.getPath());

                int responseType = response.getType();
                if (responseType == Response.TYPE_PASSTHROUGH
                        || responseType == Response.TYPE_FORWARD) {
                    // 画面描画のためのAction呼び出しを行なう。
                    // （画面描画のためのAction呼び出しの際にはAction付随の制約以外の
                    // 制約チェックを行なわない。）
                    invokeAction(component, getActionMethod(component,
                            ACTION_RENDER, null, false, request), request,
                            false);
                }

                finishForComponent(component, request);
            } else {
                // componentがnullになるのは、component名が割り当てられているのにまだ
                // 対応するcomponentクラスが作成されていない場合。
                // その場合自動生成機能でクラスやテンプレートの自動生成が適切にできるように、
                // デフォルト値からResponseを作るようにしている。
                // （例えば、リクエストパス名がテンプレートパス名ではない場合に、リクエストパス名で
                // テンプレートが作られてしまうとうれしくない。）

                response = normalizeResponse(constructDefaultResponse(request,
                        component), request.getPath());
            }
        }

        if (log_.isDebugEnabled()) {
            log_.debug("FINAL RESPONSE: " + response);
        }

        if (ymir_.isUnderDevelopment()) {
            for (int i = 0; i < updaters_.length; i++) {
                Response newResponse = updaters_[i].update(request, response);
                if (newResponse != response) {
                    return newResponse;
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

    Notes confirmConstraint(Object component, Method action, Request request)
            throws PermissionDeniedException {

        boolean validationFailed = false;
        Notes notes = new Notes();
        Constraint[] constraint = annotationHandler_.getConstraints(component,
                action);
        for (int i = 0; i < constraint.length; i++) {
            try {
                constraint[i].confirm(component, request);
            } catch (PermissionDeniedException ex) {
                throw ex;
            } catch (ValidationFailedException ex) {
                validationFailed = true;
                ConstraintViolatedException.Message[] messages = ex
                        .getMessages();
                for (int j = 0; j < messages.length; j++) {
                    notes.add(new Note(messages[j].getKey(), messages[j]
                            .getParameters()));
                }
            } catch (ConstraintViolatedException ex) {
                throw new RuntimeException("May logic error", ex);
            }
        }
        if (validationFailed) {
            return notes;
        } else {
            return null;
        }
    }

    Object getRequestComponent(Request request) {

        return request.getAttribute(ATTR_SELF);
    }

    void prepareForComponent(Object component, Request request) {
        // リクエストパラメータをinjectする。
        BeanUtils
                .populate(beanUtilsBean_, component, request.getParameterMap());

        // FormFileのリクエストパラメータをinjectする。
        BeanUtils.copyProperties(beanUtilsBean_, component, request
                .getFileParameterMap());

        // 各コンテキストが持つ属性をinjectする。
        // （リクエストパラメータによって予期せぬinjectがあった場合にそれを上書きできるように、
        // リクエストパラメータのinjectよりも後に行なっている。）
        injectContextAttributes(component);
    }

    void finishForComponent(Object component, Request request) {
        // 各コンテキストに属性をoutjectする。
        outjectContextAttributes(component);

        // コンポーネント自体をattributeとしてバインドしておく。
        request.setAttribute(ATTR_SELF, component);
    }

    void injectContextAttributes(Object component) {
        ScopeAttribute[] attributes = annotationHandler_
                .getInjectedScopeAttributes(component);
        for (int i = 0; i < attributes.length; i++) {
            attributes[i].injectTo(component);
        }
    }

    void outjectContextAttributes(Object component) {
        ScopeAttribute[] attributes = annotationHandler_
                .getOutjectedScopeAttributes(component);
        for (int i = 0; i < attributes.length; i++) {
            attributes[i].outjectFrom(component);
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
            boolean confirmConstraints) throws PermissionDeniedException {

        Response response = PassthroughResponse.INSTANCE;
        Object[] params = new Object[0];

        if (confirmConstraints) {
            // 制約チェックを行なう。
            try {
                Notes notes = confirmConstraint(component, action, request);
                if (notes != null) {
                    request.setAttribute(ATTR_NOTES, notes);

                    // バリデーションエラーが発生した場合は、エラー処理メソッドが存在すればそれを呼び出す。
                    // メソッドが存在しなければ何もしない（元のアクションメソッドの呼び出しをスキップする）。
                    action = getActionMethod(component,
                            ACTION_VALIDATIONFAILED,
                            new Class[] { Notes.class });
                    if (action != null) {
                        params = new Object[] { notes };
                    } else {
                        action = getActionMethod(component,
                                ACTION_VALIDATIONFAILED);
                    }
                }
            } catch (PermissionDeniedException ex) {
                // 権限エラーが発生した場合は、エラー処理メソッドが存在すればそれを呼び出す。
                // メソッドが存在しなければPermissionDeniedExceptionを上に再スローする。
                action = getActionMethod(component, ACTION_PERMISSIONDENIED,
                        new Class[] { PermissionDeniedException.class });
                if (action != null) {
                    params = new Object[] { ex };
                } else {
                    action = getActionMethod(component, ACTION_PERMISSIONDENIED);
                }
                if (action == null) {
                    throw ex;
                }
            }
        }

        if (action != null) {
            if (log_.isDebugEnabled()) {
                log_.debug("INVOKE: " + component.getClass().getName() + "#"
                        + action);
            }
            Object returnValue;
            try {
                returnValue = action.invoke(component, params);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                throw new WrappingRuntimeException(ex.getCause() != null ? ex
                        .getCause() : ex);
            }
            response = constructResponse(component, action.getReturnType(),
                    returnValue);
            if (log_.isDebugEnabled()) {
                log_.debug("RESPONSE: " + response);
            }
        }

        return response;
    }

    Method getActionMethod(Object component, String actionName,
            String defaultActionName, boolean dispatchingByParamter,
            Request request) {
        if (dispatchingByParamter) {
            Method[] methods = component.getClass().getMethods();
            if (log_.isDebugEnabled()) {
                log_
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
                        if (log_.isDebugEnabled()) {
                            log_.debug("getActionMethod: Found: " + methods[i]);
                        }
                        return methods[i];
                    }
                }
                list.add(methods[i]);
            }
            if (defaultActionName != null) {
                if (log_.isDebugEnabled()) {
                    log_.debug("getActionMethod: search "
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
                            if (log_.isDebugEnabled()) {
                                log_.debug("getActionMethod: Found: " + method);
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

        if (log_.isDebugEnabled()) {
            log_.debug("getActionMethod: Found: " + method);
        }
        return method;
    }

    Response adjustResponse(Request request, Response response, Object component) {
        if (response.getType() == Response.TYPE_PASSTHROUGH) {
            response = constructDefaultResponse(request, component);
        }

        if (log_.isDebugEnabled()) {
            log_.debug("[4]RESPONSE: " + response);
        }

        return response;
    }

    Response constructDefaultResponse(Request request, Object component) {

        if (fileResourceExists(request.getPath())) {
            // パスに対応するテンプレートファイルが存在する場合はパススルーする。
            return PassthroughResponse.INSTANCE;
        } else {
            Object returnValue = request.getDefaultReturnValue();
            if (returnValue != null) {
                return constructResponse(component, returnValue.getClass(),
                        returnValue);
            } else {
                return PassthroughResponse.INSTANCE;
            }
        }
    }

    boolean fileResourceExists(String path) {

        if (path.length() == 0 || path.equals("/")) {
            return false;
        }
        String normalized;
        if (path.endsWith("/")) {
            normalized = path.substring(0, path.length() - 1);
        } else {
            normalized = path;
        }
        Set resourceSet = getServletContext().getResourcePaths(
                normalized.substring(0, normalized.lastIndexOf('/') + 1));
        return (resourceSet != null && resourceSet.contains(normalized));
    }

    public Method getActionMethod(Object component, String actionName) {

        return getActionMethod(component, actionName, new Class[0]);
    }

    public Method getActionMethod(Object component, String actionName,
            Class[] paramTypes) {

        try {
            return component.getClass().getMethod(actionName, paramTypes);
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

    public void setYmir(Ymir ymir) {
        ymir_ = ymir;
    }

    public void setResponseConstructorSelector(
            ResponseConstructorSelector responseConstructorSelector) {

        responseConstructorSelector_ = responseConstructorSelector;
    }

    public void setUpdaters(Updater[] updaters) {

        updaters_ = updaters;
    }

    public void setAnnotationHandler(AnnotationHandler annotationHandler) {

        annotationHandler_ = annotationHandler;
    }

    public void setRedirectionPathResolver(
            RedirectionPathResolver redirectionPathResolver) {

        redirectionPathResolver_ = redirectionPathResolver;
    }
}
