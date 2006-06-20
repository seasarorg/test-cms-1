package org.seasar.cms.framework.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.seasar.cms.framework.MatchedPathMapping;
import org.seasar.cms.framework.PageNotFoundException;
import org.seasar.cms.framework.PathMapping;
import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.RequestProcessor;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.container.ThreadLocalS2ContainerUtils;
import org.seasar.cms.framework.creator.SourceCreator;
import org.seasar.cms.framework.response.constructor.ResponseConstructor;
import org.seasar.cms.framework.response.constructor.ResponseConstructorSelector;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.S2Container;
import org.seasar.kvasir.util.el.VariableResolver;

public class DefaultRequestProcessor implements RequestProcessor {

    public static final String ACTION_RENDER = "_render";

    public static final String ATTR_PAGE = "PAGE";

    private PathMapping[] mappings_;

    private PathMapping[] ignoreMappings_;

    private ResponseConstructorSelector responseConstructorSelector_;

    private S2Container container_;

    private SourceCreator sourceCreator_;

    public Response process(String contextPath, String path, String method,
        String dispatcher, Map parameterMap) throws PageNotFoundException {

        if (isPathIgnored(path, method, dispatcher)) {
            throw new PageNotFoundException(path);
        }

        MatchedPathMapping matched = findMatchedPathMapping(path, method);
        if (matched == null) {
            return new VoidResponse();
        }
        PathMapping mapping = matched.getPathMapping();
        VariableResolver resolver = matched.getVariableResolver();

        String componentName = mapping.getComponentName(resolver);
        String actionName = mapping.getActionName(resolver);
        Request request = new RequestImpl(contextPath, path, method,
            dispatcher, parameterMap, mapping.getPathInfo(resolver));

        if (sourceCreator_ != null) {
            Response response = sourceCreator_.update(path,
                request.getMethod(), request);
            if (response != null) {
                return response;
            }
        }

        return processRequest(request, componentName, actionName);
    }

    public MatchedPathMapping findMatchedPathMapping(String path, String method) {

        VariableResolver resolver = null;
        if (mappings_ != null) {
            for (int i = 0; i < mappings_.length; i++) {
                resolver = mappings_[i].match(path, method);
                if (resolver != null) {
                    return new MatchedPathMapping(mappings_[i], resolver);
                }
            }
        }
        return null;
    }

    boolean isPathIgnored(String path, String method, String dispatcher)
        throws PageNotFoundException {

        if (Request.DISPATCHER_REQUEST.equals(dispatcher)
            && ignoreMappings_ != null) {
            for (int i = 0; i < ignoreMappings_.length; i++) {
                if (ignoreMappings_[i].match(path, method) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    Response processRequest(Request request, String componentName,
        String actionName) {

        if (!container_.hasComponentDef(componentName)) {
            return PassthroughResponse.INSTANCE;
        }

        Object component;
        try {
            ThreadLocalS2ContainerUtils.register(container_, request);
            component = container_.getComponent(componentName);
        } finally {
            ThreadLocalS2ContainerUtils.deregister(container_, request);
        }

        Response response = PassthroughResponse.INSTANCE;

        if (component != ((HttpServletRequest) container_.getExternalContext()
            .getRequest()).getAttribute(ATTR_PAGE)) {
            // 同一リクエストで直前に同一コンポーネントについて処理済みの
            // 場合はリクエストパラメータのinjectionもrenderメソッドの
            // 呼び出しもしない。そうでない場合のみ処理を行なう。

            try {
                BeanUtils.populate(component, request.getParameterMap());
            } catch (Throwable t) {
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
            ((HttpServletRequest) container_.getExternalContext().getRequest())
                .setAttribute(ATTR_PAGE, component);
        }

        return response;
    }

    Response invokeAction(Object component, String actionName) {

        Method method = getActionMethod(component, actionName);
        if (method == null) {
            return PassthroughResponse.INSTANCE;
        }

        try {
            return constructResponse(method.getReturnType(), method.invoke(
                component, new Object[0]));
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
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

    Response constructResponse(Class type, Object value) {

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

        return constructor.constructResponse(value);
    }

    public PathMapping[] getPathMappings() {

        return mappings_;
    }

    public void addMapping(String patternString, String componentNameTemplate,
        String actionNameTemplate, String pathInfoTemplate) {

        mappings_ = addMapping(mappings_, new PathMapping(patternString,
            componentNameTemplate, actionNameTemplate, pathInfoTemplate));
    }

    public PathMapping[] getIgnorePathMappings() {

        return ignoreMappings_;
    }

    public void addIgnoreMapping(String patternString) {

        ignoreMappings_ = addMapping(ignoreMappings_, new PathMapping(
            patternString, null, null, null));
    }

    PathMapping[] addMapping(PathMapping[] patterns, PathMapping pattern) {

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

    public void setSourceCreator(SourceCreator sourceCreator) {

        sourceCreator_ = sourceCreator;
    }
}
