package org.seasar.cms.framework.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.seasar.cms.framework.PageNotFoundRuntimeException;
import org.seasar.cms.framework.PathMapping;
import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.RequestProcessor;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.ResponseConstructor;
import org.seasar.cms.framework.ResponseConstructorSelector;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.S2Container;
import org.seasar.kvasir.util.el.TextTemplateEvaluator;
import org.seasar.kvasir.util.el.VariableResolver;
import org.seasar.kvasir.util.el.impl.SimpleTextTemplateEvaluator;

public class DefaultRequestProcessor implements RequestProcessor {

    private S2Container container_;

    private TextTemplateEvaluator evaluator_ = new SimpleTextTemplateEvaluator();

    private PathMapping[] mappings_;

    private PathMapping[] ignoreMappings_;

    private ResponseConstructorSelector responseConstructorSelector_;

    public Response process(String path, String method, String dispatcher,
        Map parameterMap) {

        checkIfPathShouldBeIgnored(path, dispatcher);

        PathMapping mapping = null;
        VariableResolver resolver = null;
        if (mappings_ != null) {
            for (int i = 0; i < mappings_.length; i++) {
                resolver = mappings_[i].match(path);
                if (resolver != null) {
                    mapping = mappings_[i];
                    break;
                }
            }
        }
        if (mapping == null) {
            return null;
        }

        Request request = new RequestImpl(path, method, dispatcher,
            parameterMap, mapping.getPathInfo(resolver));
        String componentName = mapping.getComponentName(resolver);
        String actionName = mapping.getActionName(resolver);

        return processRequest(request, componentName, actionName);
    }

    void checkIfPathShouldBeIgnored(String path, String dispatcher)
        throws PageNotFoundRuntimeException {

        if (DISPATCHER_REQUEST.equals(dispatcher) && ignoreMappings_ != null) {
            for (int i = 0; i < ignoreMappings_.length; i++) {
                if (ignoreMappings_[i].match(path) != null) {
                    throw new PageNotFoundRuntimeException(path);
                }
            }
        }
    }

    Response processRequest(Request request, String componentName,
        String actionName) {

        if (!container_.hasComponentDef(componentName)) {
            return null;
        }
        Object component = container_.getComponent(componentName);

        try {
            BeanUtils.populate(component, request.getParameterMap());
        } catch (IllegalAccessException ex) {
        } catch (InvocationTargetException ex) {
        }

        // TODO componentにrequestをinjectしたい！

        Method method = getActionMethod(component, actionName);
        if (method == null) {
            return null;
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

    Response constructResponse(Class type, Object value) {

        if (type == Void.TYPE) {
            return null;
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

    Method getActionMethod(Object component, String actionName) {

        return null;
    }

    public void addMapping(String patternString, String componentNameTemplate,
        String actionNameTemplate, String pathInfoTemplate) {

        mappings_ = addMapping(mappings_, new PathMapping(patternString,
            componentNameTemplate, actionNameTemplate, pathInfoTemplate));
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

    public void setContainer(S2Container container) {

        container_ = container;
    }
}
